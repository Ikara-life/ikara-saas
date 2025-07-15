package studio.ikara.commons.service;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheObject implements Serializable {

    @Serial
    private static final long serialVersionUID = 1616868309218812235L;

    private Object object; // NOSONAR
}
