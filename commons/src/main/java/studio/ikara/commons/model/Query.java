package studio.ikara.commons.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import studio.ikara.commons.model.condition.AbstractCondition;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class Query implements Serializable {

    @Serial
    private static final long serialVersionUID = 8907315335358221972L;

    public static final Sort DEFAULT_SORT = Sort.by(Order.desc("updatedAt"));

    private AbstractCondition condition;
    private int size = 10;
    private int page = 0;
    private Sort sort = DEFAULT_SORT;
    private Boolean count = Boolean.TRUE;
    private List<String> fields;
    private Boolean excludeFields = Boolean.FALSE;
    private Boolean eager = Boolean.FALSE;
    private List<String> eagerFields;

    public Pageable getPageable() {
        return PageRequest.of(this.page, this.size, this.sort);
    }
}
