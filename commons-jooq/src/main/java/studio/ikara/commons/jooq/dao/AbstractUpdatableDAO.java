package studio.ikara.commons.jooq.dao;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.springframework.transaction.annotation.Transactional;
import studio.ikara.commons.model.dto.AbstractUpdatableDTO;
import studio.ikara.commons.thread.VirtualThreadExecutor;

@Transactional
public abstract class AbstractUpdatableDAO<
                R extends UpdatableRecord<R>, I extends Serializable, D extends AbstractUpdatableDTO<I, I>>
        extends AbstractDAO<R, I, D> {

    private static final String UPDATED_BY = "UPDATED_BY";

    protected final Field<?> updatedByField;

    protected AbstractUpdatableDAO(Class<D> pojoClass, Table<R> table, Field<I> idField) {
        super(pojoClass, table, idField);
        this.updatedByField = table.field(UPDATED_BY);
    }

    public <A extends AbstractUpdatableDTO<I, I>> CompletableFuture<D> update(A entity) {
        return VirtualThreadExecutor.async(() -> {
            entity.setUpdatedAt(null);
            UpdatableRecord<R> rec = this.dslContext.newRecord(this.table);
            rec.from(entity);
            rec.reset("CREATED_BY");
            rec.reset("CREATED_AT");

            this.dslContext
                    .update(this.table)
                    .set(rec)
                    .where(this.idField.eq(entity.getId()))
                    .execute();

            return rec.into(this.pojoClass);
        });
    }

    @SuppressWarnings("unchecked")
    public CompletableFuture<D> update(I id, Map<String, Object> updateFields) {
        return VirtualThreadExecutor.async(() -> {
            updateFields.remove("createdAt");

            Map<Field<?>, Object> fields = updateFields.entrySet().stream()
                    .map(e -> Map.entry(this.getField(e.getKey()), e.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            this.dslContext
                    .update(this.table)
                    .set(fields)
                    .where(this.idField.eq(id))
                    .execute();

            return this.getRecordById(id).into(this.pojoClass);
        });
    }
}
