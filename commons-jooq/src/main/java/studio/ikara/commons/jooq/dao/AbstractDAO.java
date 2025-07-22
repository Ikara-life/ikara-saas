package studio.ikara.commons.jooq.dao;

import studio.ikara.commons.configuration.service.AbstractMessageService;
import studio.ikara.commons.exception.GenericException;
import studio.ikara.commons.function.Tuple2;
import studio.ikara.commons.function.Tuples;
import studio.ikara.commons.model.condition.AbstractCondition;
import studio.ikara.commons.model.condition.ComplexCondition;
import studio.ikara.commons.model.condition.ComplexConditionOperator;
import studio.ikara.commons.model.condition.FilterCondition;
import studio.ikara.commons.model.condition.FilterConditionOperator;
import studio.ikara.commons.model.dto.AbstractDTO;
import studio.ikara.commons.thread.VirtualThreadExecutor;
import lombok.Getter;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.DeleteQuery;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.SelectJoinStep;
import org.jooq.SortField;
import org.jooq.SortOrder;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public abstract class AbstractDAO<R extends UpdatableRecord<R>, I extends Serializable, D extends AbstractDTO<I, I>> {

    private static final String OBJECT_NOT_FOUND = AbstractMessageService.OBJECT_NOT_FOUND;

    @Getter
    protected final Class<D> pojoClass;

    protected final Logger logger;
    protected final Table<R> table;
    protected final Field<I> idField;

    @Autowired // NOSONAR
    protected DSLContext dslContext;

    @Autowired // NOSONAR
    protected AbstractMessageService messageResourceService;

    protected AbstractDAO(Class<D> pojoClass, Table<R> table, Field<I> idField) {
        this.pojoClass = pojoClass;
        this.table = table;
        this.idField = idField;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public CompletableFuture<Page<D>> readPage(Pageable pageable) {
        return VirtualThreadExecutor.async(() -> {
            Tuple2<SelectJoinStep<org.jooq.Record>, SelectJoinStep<Record1<Integer>>> selectJoinStepTuple =
                    getSelectJointStep();
            return list(pageable, selectJoinStepTuple);
        });
    }

    @SuppressWarnings("unchecked")
    public CompletableFuture<Page<D>> readPageFilter(Pageable pageable, AbstractCondition condition) {
        return VirtualThreadExecutor.async(() -> {
            Tuple2<SelectJoinStep<org.jooq.Record>, SelectJoinStep<Record1<Integer>>> selectJoinStepTuple =
                    getSelectJointStep();
            Condition filterCondition = filter(condition);
            return list(
                    pageable,
                    Tuples.of(
                            (SelectJoinStep<org.jooq.Record>)
                                    selectJoinStepTuple.getT1().where(filterCondition),
                            (SelectJoinStep<Record1<Integer>>)
                                    selectJoinStepTuple.getT2().where(filterCondition)));
        });
    }

    protected Page<D> list(
            Pageable pageable,
            Tuple2<SelectJoinStep<org.jooq.Record>, SelectJoinStep<Record1<Integer>>> selectJoinStepTuple) {
        List<SortField<?>> orderBy = new ArrayList<>();

        pageable.getSort().forEach(order -> {
            Field<?> field = this.getField(order.getProperty());
            if (field != null)
                orderBy.add(field.sort(order.getDirection() == Sort.Direction.ASC ? SortOrder.ASC : SortOrder.DESC));
        });

        final Integer recordsCount = selectJoinStepTuple.getT2().fetchOne().value1();

        SelectJoinStep<org.jooq.Record> selectJoinStep = selectJoinStepTuple.getT1();
        if (!orderBy.isEmpty()) {
            selectJoinStep.orderBy(orderBy);
        }

        List<D> recordsList = selectJoinStep
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch()
                .map(e -> e.into(this.pojoClass));

        return PageableExecutionUtils.getPage(recordsList, pageable, () -> recordsCount);
    }

    public CompletableFuture<List<D>> readAll(AbstractCondition query) {
        return VirtualThreadExecutor.async(() -> {
            SelectJoinStep<org.jooq.Record> selectJoinStep =
                    getSelectJointStep().getT1();
            Condition condition = filter(query);
            selectJoinStep.where(condition);
            return selectJoinStep.fetch().map(e -> e.into(this.pojoClass));
        });
    }

    public CompletableFuture<D> readById(I id) {
        return VirtualThreadExecutor.async(() -> this.getRecordById(id).into(this.pojoClass));
    }

    public CompletableFuture<D> create(D pojo) {
        return VirtualThreadExecutor.async(() -> {
            pojo.setId(null);

            return dslContext.transactionResult(ctx -> {
                DSLContext dsl = ctx.dsl();

                R rec = dsl.newRecord(this.table);
                rec.from(pojo);

                I id = dsl.insertInto(this.table)
                        .set(rec)
                        .returning(this.idField)
                        .fetchOne()
                        .getValue(this.idField);

                return dsl.selectFrom(this.table)
                        .where(this.idField.eq(id))
                        .limit(1)
                        .fetchOne()
                        .into(this.pojoClass);
            });
        });
    }

    public CompletableFuture<Integer> delete(I id) {
        return VirtualThreadExecutor.async(() -> {
            DeleteQuery<R> query = dslContext.deleteQuery(table);
            query.addConditions(idField.eq(id));
            return query.execute();
        });
    }

    protected Condition filter(AbstractCondition condition) {
        if (condition == null) return DSL.noCondition();

        Condition cond;
        if (condition instanceof ComplexCondition cc) cond = complexConditionFilter(cc);
        else cond = filterConditionFilter((FilterCondition) condition);

        return condition.isNegate() ? cond.not() : cond;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Condition filterConditionFilter(FilterCondition fc) {
        Field field = this.getField(fc.getField());

        if (field == null) return DSL.noCondition();

        if (fc.getOperator() == FilterConditionOperator.BETWEEN)
            return field.between(
                            fc.isValueField()
                                    ? (Field<?>) this.getField(fc.getField())
                                    : this.fieldValue(field, fc.getValue()))
                    .and(
                            fc.isToValueField()
                                    ? (Field<?>) this.getField(fc.getField())
                                    : this.fieldValue(field, fc.getToValue()));

        if (fc.getOperator() == FilterConditionOperator.EQUALS
                || fc.getOperator() == FilterConditionOperator.GREATER_THAN
                || fc.getOperator() == FilterConditionOperator.GREATER_THAN_EQUAL
                || fc.getOperator() == FilterConditionOperator.LESS_THAN
                || fc.getOperator() == FilterConditionOperator.LESS_THAN_EQUAL) {
            if (fc.isValueField()) {
                if (fc.getField() == null) return DSL.noCondition();
                return switch (fc.getOperator()) {
                    case EQUALS -> field.eq(this.getField(fc.getField()));
                    case GREATER_THAN -> field.gt(this.getField(fc.getField()));
                    case GREATER_THAN_EQUAL -> field.ge(this.getField(fc.getField()));
                    case LESS_THAN -> field.lt(this.getField(fc.getField()));
                    case LESS_THAN_EQUAL -> field.le(this.getField(fc.getField()));
                    default -> DSL.noCondition();
                };
            }

            if (fc.getValue() == null) return DSL.noCondition();
            Object v = this.fieldValue(field, fc.getValue());
            return switch (fc.getOperator()) {
                case EQUALS -> field.eq(this.fieldValue(field, v));
                case GREATER_THAN -> field.gt(this.fieldValue(field, v));
                case GREATER_THAN_EQUAL -> field.ge(this.fieldValue(field, v));
                case LESS_THAN -> field.lt(this.fieldValue(field, v));
                case LESS_THAN_EQUAL -> field.le(this.fieldValue(field, v));
                default -> DSL.noCondition();
            };
        }

        return switch (fc.getOperator()) {
            case IS_FALSE -> field.isFalse();
            case IS_TRUE -> field.isTrue();
            case IS_NULL -> field.isNull();
            case IN -> field.in(this.multiFieldValue(field, fc.getValue(), fc.getMultiValue()));
            case LIKE -> field.like(fc.getValue().toString());
            case STRING_LOOSE_EQUAL -> field.like("%" + fc.getValue() + "%");
            default -> DSL.noCondition();
        };
    }

    private List<?> multiFieldValue(Field<?> field, Object obValue, List<?> values) {
        if (values != null && !values.isEmpty()) return values;

        if (obValue == null) return List.of();

        int from = 0;
        String iValue = obValue.toString().trim();

        List<Object> obj = new ArrayList<>();
        for (int i = 0; i < iValue.length(); i++) {
            if (iValue.charAt(i) != ',') continue;

            if (i != 0 && iValue.charAt(i - 1) == '\\') continue;

            String str = iValue.substring(from, i).trim();
            if (str.isEmpty()) continue;

            obj.add(this.fieldValue(field, str));
            from = i + 1;
        }

        return obj;
    }

    private Object fieldValue(Field<?> field, Object value) {
        if (value == null) return null;

        DataType<?> dt = field.getDataType();

        if (dt.isString() || dt.isJSON() || dt.isEnum()) return value.toString();

        if (dt.isNumeric()) {
            if (value instanceof Number) return value;

            if (dt.hasPrecision()) return Double.valueOf(value.toString());

            return Long.valueOf(value.toString());
        }

        if (dt.isDate() || dt.isDateTime() || dt.isTime() || dt.isTimestamp())
            return value.equals("now")
                    ? LocalDateTime.now()
                    : LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(value.toString())), ZoneId.of("UTC"));

        return value;
    }

    protected Condition complexConditionFilter(ComplexCondition cc) {
        if (cc.getConditions() == null || cc.getConditions().isEmpty()) return DSL.noCondition();

        List<Condition> conditions =
                cc.getConditions().stream().map(this::filter).toList();

        return cc.getOperator() == ComplexConditionOperator.AND ? DSL.and(conditions) : DSL.or(conditions);
    }

    protected org.jooq.Record getRecordById(I id) {
        org.jooq.Record rc =
                this.getSelectJointStep().getT1().where(idField.eq(id)).fetchOne();

        if (rc == null) {
            String msg = messageResourceService.getMessage(OBJECT_NOT_FOUND, this.pojoClass.getSimpleName(), id);
            throw new GenericException(HttpStatus.NOT_FOUND, msg);
        }

        return rc;
    }

    protected Tuple2<SelectJoinStep<org.jooq.Record>, SelectJoinStep<Record1<Integer>>> getSelectJointStep() {
        return Tuples.of(
                dslContext.select(Arrays.asList(table.fields())).from(table),
                dslContext.select(DSL.count()).from(table));
    }

    @SuppressWarnings("rawtypes")
    protected Field getField(String fieldName) {
        return table.field(convertToJOOQFieldName(fieldName));
    }

    protected String convertToJOOQFieldName(String fieldName) {
        return fieldName.replaceAll("([A-Z])", "_$1").toUpperCase();
    }
}
