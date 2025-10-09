package studio.ikara.security.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import studio.ikara.commons.model.dto.AbstractUpdatableDTO;
import studio.ikara.security.enums.CoachTypeCode;
import studio.ikara.commons.security.jwt.ContextUser;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ToString(callSuper = true)
public class Coach extends AbstractUpdatableDTO<Long, Long> {

    @Serial
    private static final long serialVersionUID = 802341134013L;

    private String coachId;
    private String name;
    private String emailId;
    private String phoneNumber;
    private boolean fromEntity;
    private Long entityId;
    private CoachTypeCode coachTypeCode;
    private Long userId;
    private LocalDateTime joinedAt;

    @JsonIgnore
    public ContextUser toContextUser() {
        return new ContextUser()
                .setId(userId)
                .setUserName(name)
                .setEmailId(emailId)
                .setPhoneNumber(phoneNumber)
                .setCoach(true)
                .setCoachId(coachId)
                .setCreatedAt(getCreatedAt())
                .setUpdatedAt(getUpdatedAt());
    }
}