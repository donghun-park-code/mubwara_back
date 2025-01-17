package site.metacoding.finals.domain.customer;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.metacoding.finals.domain.AutoTime;
import site.metacoding.finals.domain.reservation.Reservation;
import site.metacoding.finals.domain.user.User;
import site.metacoding.finals.dto.customer.CustomerReqDto.CustomerUpdateReqDto;

// @DynamicInsert
@SQLDelete(sql = "UPDATE reservation SET is_deleted = true where id = ?")
@Where(clause = "is_deleted = false")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "customer")
@Getter
public class Customer extends AutoTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 20)
    private String name;
    @Column(nullable = false, length = 12)
    private String phoneNumber;
    @Column(length = 30)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // @ColumnDefault("false")
    @Column(name = "is_deleted", insertable = false) // 이것도 안 먹음 => 인서트할 때 직접 넣어줌
    private Boolean isDeleted;

    // @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    // private List<Reservation> reservation = new ArrayList<>();

    public Customer toEntity(CustomerUpdateReqDto dto) {
        return Customer.builder()
                .id(this.id)
                .name(dto.getName())
                .address(dto.getAddress())
                .phoneNumber(dto.getPhoneNumber())
                .user(this.user)
                .build();
    }

}
