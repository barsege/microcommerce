package com.barsege.paymentservice.provider;

import com.barsege.paymentservice.dto.PaymentRequest;
import com.barsege.paymentservice.dto.PaymentResult;
import com.iyzipay.Options;
import com.iyzipay.model.Address;
import com.iyzipay.model.BasketItem;
import com.iyzipay.model.BasketItemType;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.Currency;
import com.iyzipay.model.Locale;
import com.iyzipay.model.PaymentCard;
import com.iyzipay.model.PaymentChannel;
import com.iyzipay.model.PaymentGroup;
import com.iyzipay.model.Status;
import com.iyzipay.request.CreatePaymentRequest;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "iyzico")
@Slf4j
public class IyzicoPaymentProvider implements PaymentProvider {

    private final String apiKey;
    private final String secretKey;
    private final String baseUrl;
    private final String testCardHolderName;
    private final String testCardNumber;
    private final String testCardExpireMonth;
    private final String testCardExpireYear;
    private final String testCardCvc;

    public IyzicoPaymentProvider(
            @Value("${iyzico.api-key}") String apiKey,
            @Value("${iyzico.secret-key}") String secretKey,
            @Value("${iyzico.base-url}") String baseUrl,
            @Value("${iyzico.test-card.holder-name}") String testCardHolderName,
            @Value("${iyzico.test-card.number}") String testCardNumber,
            @Value("${iyzico.test-card.expire-month}") String testCardExpireMonth,
            @Value("${iyzico.test-card.expire-year}") String testCardExpireYear,
            @Value("${iyzico.test-card.cvc}") String testCardCvc
    ) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.baseUrl = baseUrl;
        this.testCardHolderName = testCardHolderName;
        this.testCardNumber = testCardNumber;
        this.testCardExpireMonth = testCardExpireMonth;
        this.testCardExpireYear = testCardExpireYear;
        this.testCardCvc = testCardCvc;
    }

    @PostConstruct
    void logProviderSelected() {
        log.info("Payment provider selected: iyzico, baseUrl={}", baseUrl);
    }

    @Override
    public PaymentResult pay(PaymentRequest request) {
        try {
            log.info("Iyzico payment request started. orderId={}, amount={}", request.orderId(), request.amount());

            com.iyzipay.model.Payment payment = com.iyzipay.model.Payment.create(
                    buildCreatePaymentRequest(request),
                    buildOptions()
            );

            if (Status.SUCCESS.getValue().equals(payment.getStatus())) {
                String transactionId = payment.getPaymentId() != null
                        ? payment.getPaymentId()
                        : payment.getConversationId();
                log.info("Iyzico payment success. orderId={}, amount={}", request.orderId(), request.amount());
                return new PaymentResult(true, transactionId, null);
            }

            String failureReason = payment.getErrorMessage() != null
                    ? payment.getErrorMessage()
                    : "Iyzico payment failed";
            log.warn("Iyzico payment failure. orderId={}, amount={}, reason={}",
                    request.orderId(), request.amount(), failureReason);
            return new PaymentResult(false, null, failureReason);
        } catch (Exception exception) {
            log.error("Iyzico payment exception. orderId={}, amount={}",
                    request.orderId(), request.amount(), exception);
            return new PaymentResult(false, null, "Iyzico payment error: " + exception.getMessage());
        }
    }

    private Options buildOptions() {
        Options options = new Options();
        options.setApiKey(apiKey);
        options.setSecretKey(secretKey);
        options.setBaseUrl(baseUrl);
        return options;
    }

    private CreatePaymentRequest buildCreatePaymentRequest(PaymentRequest paymentRequest) {
        BigDecimal amount = paymentRequest.amount();
        String orderId = String.valueOf(paymentRequest.orderId());

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setLocale(Locale.TR.getValue());
        request.setConversationId(orderId);
        request.setPrice(amount);
        request.setPaidPrice(amount);
        request.setCurrency(resolveCurrency(paymentRequest.currency()));
        request.setInstallment(1);
        request.setBasketId(orderId);
        request.setPaymentChannel(PaymentChannel.WEB.name());
        request.setPaymentGroup(PaymentGroup.PRODUCT.name());
        request.setPaymentCard(buildPaymentCard(paymentRequest));
        request.setBuyer(buildBuyer(paymentRequest));
        request.setBillingAddress(buildAddress());
        request.setShippingAddress(buildAddress());
        request.setBasketItems(List.of(buildBasketItem(orderId, amount)));
        return request;
    }

    private PaymentCard buildPaymentCard(PaymentRequest request) {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCardHolderName(valueOrDefault(request.cardHolderName(), testCardHolderName));
        paymentCard.setCardNumber(valueOrDefault(request.cardNumber(), testCardNumber));
        paymentCard.setExpireMonth(valueOrDefault(request.expireMonth(), testCardExpireMonth));
        paymentCard.setExpireYear(valueOrDefault(request.expireYear(), testCardExpireYear));
        paymentCard.setCvc(valueOrDefault(request.cvc(), testCardCvc));
        paymentCard.setRegisterCard(0);
        return paymentCard;
    }

    private Buyer buildBuyer(PaymentRequest request) {
        String userId = valueOrDefault(request.userId(), "guest");

        Buyer buyer = new Buyer();
        buyer.setId(userId);
        buyer.setName("Mini");
        buyer.setSurname("Commerce");
        buyer.setGsmNumber("+905350000000");
        buyer.setEmail(userId + "@example.com");
        buyer.setIdentityNumber("74300864791");
        buyer.setLastLoginDate("2026-01-01 12:00:00");
        buyer.setRegistrationDate("2026-01-01 12:00:00");
        buyer.setRegistrationAddress("Demo Address");
        buyer.setIp("127.0.0.1");
        buyer.setCity("Istanbul");
        buyer.setCountry("Turkey");
        buyer.setZipCode("34000");
        return buyer;
    }

    private Address buildAddress() {
        Address address = new Address();
        address.setContactName("Mini Commerce");
        address.setCity("Istanbul");
        address.setCountry("Turkey");
        address.setAddress("Demo Address");
        address.setZipCode("34000");
        return address;
    }

    private BasketItem buildBasketItem(String orderId, BigDecimal amount) {
        BasketItem basketItem = new BasketItem();
        basketItem.setId("ORDER-" + orderId);
        basketItem.setName("Mini Commerce Order");
        basketItem.setCategory1("General");
        basketItem.setItemType(BasketItemType.PHYSICAL.name());
        basketItem.setPrice(amount);
        return basketItem;
    }

    private String resolveCurrency(String currency) {
        if (Currency.USD.name().equalsIgnoreCase(currency)) {
            return Currency.USD.name();
        }
        if (Currency.EUR.name().equalsIgnoreCase(currency)) {
            return Currency.EUR.name();
        }
        return Currency.TRY.name();
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    @Override
    public com.barsege.paymentservice.entity.PaymentProvider providerType() {
        return com.barsege.paymentservice.entity.PaymentProvider.IYZICO;
    }
}
