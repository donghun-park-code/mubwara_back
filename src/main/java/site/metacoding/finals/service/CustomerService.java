package site.metacoding.finals.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.metacoding.finals.config.auth.PrincipalUser;
import site.metacoding.finals.domain.customer.Customer;
import site.metacoding.finals.domain.customer.CustomerRepository;
import site.metacoding.finals.domain.reservation.Reservation;
import site.metacoding.finals.domain.reservation.ReservationRepository;
import site.metacoding.finals.domain.review.Review;
import site.metacoding.finals.domain.review.ReviewRepository;
import site.metacoding.finals.domain.shop.Shop;
import site.metacoding.finals.domain.shop.ShopRepository;
import site.metacoding.finals.domain.subscribe.SubscribeRepository;
import site.metacoding.finals.domain.user.User;
import site.metacoding.finals.domain.user.UserRepository;
import site.metacoding.finals.dto.customer.CustomerReqDto.CustomerJoinReqDto;
import site.metacoding.finals.dto.customer.CustomerReqDto.CustomerUpdateReqDto;
import site.metacoding.finals.dto.customer.CustomerRespDto.CustomerJoinRespDto;
import site.metacoding.finals.dto.customer.CustomerRespDto.CustomerMyPageReviewRespDto;
import site.metacoding.finals.dto.customer.CustomerRespDto.CustomerMyPageSubscribeRespDto;
import site.metacoding.finals.dto.customer.CustomerRespDto.CustomerUpdateRespDto;
import site.metacoding.finals.dto.repository.customer.ReservationRepositoryRespDto;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerService {

    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRespository;
    private final ReservationRepository reservationRepository;
    private final SubscribeRepository subscribeRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public CustomerJoinRespDto join(CustomerJoinReqDto customerJoinReqDto) {
        String password = bCryptPasswordEncoder.encode(customerJoinReqDto.getPassword());
        customerJoinReqDto.setPassword(password);

        User user = userRepository.save(customerJoinReqDto.toUserEntity());
        Customer customer = customerRepository.save(customerJoinReqDto.toCustomerEntity(user));

        return new CustomerJoinRespDto(customer, user);
    }

    @Transactional
    public CustomerUpdateRespDto update(Long id, CustomerUpdateReqDto customerUpdateReqDto) {
        Customer customerPS = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("유저 정보 찾을 수 없음"));

        customerPS.toEntity(customerUpdateReqDto);

        customerRepository.save(customerPS);

        return new CustomerUpdateRespDto(customerPS);
    }

    @Transactional
    public void delete(PrincipalUser principalUser) {
        log.debug("디버그 : " + principalUser.getUsername());
        log.debug("디버그 : " + principalUser.getUser().getId());

        Customer customerPS = customerRepository.findByUserId(principalUser.getUser().getId())
                .orElseThrow(() -> new RuntimeException("회원 정보 없음"));

        customerRepository.deleteById(customerPS.getId());
        userRepository.deleteById(principalUser.getUser().getId());

        // 구독 정보는 바로 삭제
        log.debug("여기ㅣㅣㅣㅣㅣㅣㅣㅣㅣㅣㅣㅣㅣㅣ까지왔음");
        if (subscribeRepository.findByCustomerId(customerPS.getId()) != null) {
            log.debug("여기까진 왔음?");
            subscribeRepository.deleteByCustomerId(customerPS.getId());
        }
        // 예약 정보는 소프트 딜리트
        List<Reservation> reservationPS = reservationRepository.findByCustomerId(customerPS.getId());
        if (reservationPS != null) {
            reservationPS.stream().forEach(r -> reservationRepository.deleteById(r.getId()));
        }

        // 리뷰는 삭제하지 않음

    }

    @Transactional(readOnly = true)
    public List<ReservationRepositoryRespDto> myPageReservation(Long id) {
        List<ReservationRepositoryRespDto> reservationList = shopRespository.findResevationByCustomerId(id);
        return reservationList;
    }

    @Transactional(readOnly = true)
    public List<CustomerMyPageSubscribeRespDto> myPageSubscribe(Long id) {

        List<Shop> shopList = shopRespository.findSubscribeByCustomerId(id);

        log.debug("디버그 : " + shopList.get(0).getShopName());
        log.debug("디버그 : " + shopList.get(0).getImageFile().getStoreFilename());

        List<CustomerMyPageSubscribeRespDto> respDto = new ArrayList<>();
        shopList.forEach((s) -> respDto.add(new CustomerMyPageSubscribeRespDto(s)));
        return respDto;
    }

    @Transactional(readOnly = true)
    public List<CustomerMyPageReviewRespDto> myPageReview(Long id) {
        List<Review> reviewList = reviewRepository.findByCustomerId(id);
        return reviewList.stream().map((r) -> new CustomerMyPageReviewRespDto(r)).collect(Collectors.toList());
    }
}
