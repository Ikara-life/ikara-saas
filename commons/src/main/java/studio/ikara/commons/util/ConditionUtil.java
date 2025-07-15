package studio.ikara.commons.util;

import io.j13n.commons.model.condition.AbstractCondition;
import io.j13n.commons.model.condition.ComplexCondition;
import io.j13n.commons.model.condition.ComplexConditionOperator;
import io.j13n.commons.model.condition.FilterCondition;
import io.j13n.commons.model.condition.FilterConditionOperator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConditionUtil {

    private ConditionUtil() {}

    public static AbstractCondition parameterMapToMap(Map<String, List<String>> multiValueMap) {

        List<AbstractCondition> conditions = multiValueMap.entrySet().stream()
                .map(e -> {
                    List<String> value = e.getValue();
                    if (value == null || value.isEmpty())
                        return new FilterCondition()
                                .setField(e.getKey())
                                .setOperator(FilterConditionOperator.EQUALS)
                                .setValue("");

                    if (value.size() == 1)
                        return new FilterCondition()
                                .setField(e.getKey())
                                .setOperator(FilterConditionOperator.EQUALS)
                                .setValue(value.getFirst());

                    return new FilterCondition()
                            .setField(e.getKey())
                            .setOperator(FilterConditionOperator.IN)
                            .setValue(value.stream()
                                    .map(v -> v.replace(",", "\\,"))
                                    .collect(Collectors.joining(",")));
                })
                .map(AbstractCondition.class::cast)
                .toList();

        if (conditions.isEmpty()) return null;

        if (conditions.size() == 1) return conditions.getFirst();

        return new ComplexCondition().setConditions(conditions).setOperator(ComplexConditionOperator.AND);
    }
}
