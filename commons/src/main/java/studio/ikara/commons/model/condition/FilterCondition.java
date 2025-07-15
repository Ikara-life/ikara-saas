package studio.ikara.commons.model.condition;

import io.j13n.commons.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class FilterCondition extends AbstractCondition {

    @Serial
    private static final long serialVersionUID = 6962156714385415430L;

    private String field;
    private FilterConditionOperator operator = FilterConditionOperator.EQUALS;
    private Object value; // NOSONAR
    private Object toValue; // NOSONAR
    private List<?> multiValue; // NOSONAR
    private boolean isValueField = false;
    private boolean isToValueField = false;
    private FilterConditionOperator matchOperator = FilterConditionOperator.EQUALS;

    public static FilterCondition make(String field, Object value) {
        return new FilterCondition().setField(field).setValue(value);
    }

    public static FilterCondition of(String field, Object value, FilterConditionOperator operator) {
        return new FilterCondition().setField(field).setValue(value).setOperator(operator);
    }

    @Override
    public List<FilterCondition> findConditionWithField(String fieldName) {
        return StringUtil.safeEquals(field, fieldName) ? List.of(this) : List.of();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
