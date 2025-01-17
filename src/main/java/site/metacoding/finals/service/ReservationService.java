package site.metacoding.finals.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.cache.spi.support.CollectionReadOnlyAccess;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.metacoding.finals.config.auth.PrincipalUser;
import site.metacoding.finals.domain.customer.Customer;
import site.metacoding.finals.domain.customer.CustomerRepository;
import site.metacoding.finals.domain.reservation.Reservation;
import site.metacoding.finals.domain.reservation.ReservationRepository;
import site.metacoding.finals.domain.shop.Shop;
import site.metacoding.finals.domain.shop.ShopRepository;
import site.metacoding.finals.domain.shop_table.ShopTable;
import site.metacoding.finals.domain.shop_table.ShopTableRepository;
import site.metacoding.finals.domain.user.User;
import site.metacoding.finals.domain.user.UserRepository;
import site.metacoding.finals.dto.reservation.ReservationReqDto.ReservationSaveReqDto;
import site.metacoding.finals.dto.reservation.ReservationReqDto.ReservationSelectReqDto;
import site.metacoding.finals.dto.reservation.ReservationRespDto.ReservationSaveRespDto;
import site.metacoding.finals.dto.reservation.ReservationRespDto.ReservationSelectRespDto;
import site.metacoding.finals.dto.reservation.ReservationRespDto.ReservationShopViewAllRespDto;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final CustomerRepository customerRepository;
    private final ReservationRepository reservationRepository;
    private final ShopRepository shopRespository;
    private final ShopTableRepository shopTableRepository;
    private final UserRepository userRepository;

    // shop reservation
    public List<ReservationShopViewAllRespDto> viewShopReservation(PrincipalUser principalUser) {
        Shop shopPS = shopRespository.findByUserId(principalUser.getUser().getId())
                .orElseThrow(() -> new RuntimeException());

        List<Reservation> reservationPS = reservationRepository.findCustomerByShopId(shopPS.getId());

        // log.debug("디버그 : " + reservationPS.get(0).getReservationDate());

        // return reservationPS.stream().map((r) -> new
        // ReservationShopViewAllRespDto(r)).collect(Collectors.toList());

        List<ReservationShopViewAllRespDto> result = new ArrayList<>();

        reservationPS.forEach(r -> {
            System.out.println("1");
            log.debug("디버그 유진 : " + r.getReservationDate());
            log.debug("디버그 유진 : " + r.getCustomer());
            log.debug("디버그 유진 : " + r.getShopTable());
            result.add(new ReservationShopViewAllRespDto(r));
            System.out.println("2");
        });

        return result;
    }

    // customer reservation

    public ReservationSelectRespDto personList(ReservationSelectReqDto dto) {
        List<Integer> tableList = shopTableRepository.findDistinctByShopId(dto.getShopId())
                .orElseThrow(() -> new RuntimeException("가게 테이블 없음"));
        return new ReservationSelectRespDto(null, tableList);
    }

    public List<Integer> timeList(ReservationSelectReqDto dto) {
        List<Reservation> reservationList = reservationRepository.findByDataMaxPeople(dto.getMaxPeople(),
                dto.getDate());
        Shop shopPS = shopRespository.findById(dto.getShopId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다"));

        int start = Integer.parseInt(shopPS.getOpenTime());
        int end = Integer.parseInt(shopPS.getCloseTime());

        List<Integer> timeList = new ArrayList<>();
        for (int i = start; i < end; i += shopPS.getPerHour()) {
            for (Reservation r : reservationList) {
                int disableTime = Integer.parseInt(r.getReservationTime());
                if (i == disableTime) {
                    continue;
                }
                timeList.add(i);
            }
        }
        List<Integer> distinctTime = timeList.stream().distinct().collect(Collectors.toList());

        return distinctTime;
    }

    public ReservationSaveRespDto save(ReservationSaveReqDto dto, String username) {
        User userPS = userRepository.findByUsername(username);

        log.debug("디버그 : " + userPS);
        Customer customerPS = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("잘못된 유저 회원입니다."));
        log.debug("디버그 : " + customerPS);

        ShopTable shopTablePS = shopTableRepository.findById(dto.getShopTableId())
                .orElseThrow(() -> new RuntimeException("잘못된 가게입니다"));
        log.debug("디버그 : " + shopTablePS);

        Reservation reservation = reservationRepository.save(dto.toEntity(customerPS, shopTablePS));

        return new ReservationSaveRespDto(reservation);
    }

}
