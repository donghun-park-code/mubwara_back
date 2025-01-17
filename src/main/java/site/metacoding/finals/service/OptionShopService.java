package site.metacoding.finals.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.metacoding.finals.config.auth.PrincipalUser;
import site.metacoding.finals.domain.option.Option;
import site.metacoding.finals.domain.option.OptionRepository;
import site.metacoding.finals.domain.option_shop.OptionShop;
import site.metacoding.finals.domain.option_shop.OptionShopRepository;
import site.metacoding.finals.domain.shop.Shop;
import site.metacoding.finals.domain.shop.ShopRepository;
import site.metacoding.finals.dto.option.OptionReqDto.OptionSaveReqDto;
import site.metacoding.finals.dto.option.OptionRespDto.OptionSaveRepsDto;

@Service
@RequiredArgsConstructor
public class OptionShopService {

    private final OptionShopRepository optionShopRepository;
    private final ShopRepository shopRepository;
    private final OptionRepository optionRepository;

    @Transactional
    public List<OptionSaveRepsDto> saveOption(List<OptionSaveReqDto> reqDtos, PrincipalUser principalUser) {
        // 가게 정보 찾기
        Optional<Shop> shopPS = shopRepository.findByUserId(principalUser.getUser().getId());

        System.out.println("디버그 : " + reqDtos.get(0).getOptionList());
        System.out.println("디버그 : " + optionRepository.findById(reqDtos.get(0).getOptionList()).get());

        reqDtos.forEach(r -> {
            System.out.println("디버그 : " + r.getOptionList());
        });

        List<OptionShop> optionShops = reqDtos.stream()
                .map((r) -> r.toEntity(optionRepository.findById(r.getOptionList()).get(), shopPS.get()))
                .collect(Collectors.toList());

        // optin shop에 저장
        List<OptionSaveRepsDto> result = new ArrayList<>();
        optionShops.forEach(optionShop -> {
            result.add(new OptionSaveRepsDto(optionShopRepository.save(optionShop)));
        });

        return result;
    }
}
