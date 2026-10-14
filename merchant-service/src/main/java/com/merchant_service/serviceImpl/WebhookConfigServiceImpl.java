package com.merchant_service.serviceImpl;

import com.merchant_service.dto.request.WebhookConfigRequest;
import com.merchant_service.dto.response.WebhookConfigResponse;
import com.merchant_service.entity.Merchant;
import com.merchant_service.entity.MerchantWebhookConfig;
import com.merchant_service.mapper.WebhookConfigMapper;
import com.merchant_service.repository.MerchantRepository;
import com.merchant_service.repository.WebhookConfigRepository;
import com.merchant_service.service.WebhookConfigService;
import common_lib.exceptions.ResourceNotFoundException;
import common_lib.util.RandomizerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookConfigServiceImpl implements WebhookConfigService {

    private final WebhookConfigRepository webhookConfigRepository;
    private final MerchantRepository merchantRepository;
    private final WebhookConfigMapper webhookConfigMapper;
    private final BytesEncryptor bytesEncryptor;

    /**
     * Generates a signing secret, encrypts it before persistence, and includes the
     * raw value only in the creation response.
     *
     * @throws ResourceNotFoundException if the owning merchant does not exist
     */
    @Override
    @Transactional
    public WebhookConfigResponse create(UUID merchantId, WebhookConfigRequest webhookConfigRequest) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(
                () -> new ResourceNotFoundException("MERCHANT", "Merchant not found, merchantId: " + merchantId));

        String rawSecret = RandomizerUtil.randomBase64(32);
        byte[] rawSecretBytes = rawSecret.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedSecretBytes = bytesEncryptor.encrypt(rawSecretBytes);
        String encryptedSecret = Base64.getEncoder().encodeToString(encryptedSecretBytes);

        MerchantWebhookConfig merchantWebhookConfig = MerchantWebhookConfig.builder()
                .eventTypes(webhookConfigRequest.eventTypes())
                .targetUrl(webhookConfigRequest.targetUrl())
                .merchant(merchant)
                .webhookSecret(encryptedSecret)
                .build();

        merchantWebhookConfig = webhookConfigRepository.save(merchantWebhookConfig);
        return webhookConfigMapper.toResponse(merchantWebhookConfig, rawSecret);
    }

    /**
     * Loads all configurations by merchant ID and maps each response without secret
     * material.
     */
    @Override
    public List<WebhookConfigResponse> getAll(UUID merchantId) {
        return webhookConfigRepository.findByMerchantId(merchantId).stream()
                .map(merchantWebhookConfig ->
                        webhookConfigMapper.toResponse(merchantWebhookConfig, null)).toList();
    }

    /**
     * Resolves the configuration using both IDs and maps it without secret
     * material; foreign IDs are not found.
     */
    @Override
    public WebhookConfigResponse getById(UUID merchantId, UUID webhookConfigId) {
        return webhookConfigMapper.toResponse(fetchWebhookConfig(merchantId, webhookConfigId), null);
    }

    /**
     * Replaces the target URL and subscription string in a transaction while
     * preserving the encrypted signing secret.
     */
    @Override
    @Transactional
    public WebhookConfigResponse update(UUID merchantId, UUID merchantConfigId,
                                        WebhookConfigRequest webhookConfigRequest) {
        MerchantWebhookConfig merchantWebhookConfig = fetchWebhookConfig(merchantId, merchantConfigId);
        merchantWebhookConfig.setTargetUrl(webhookConfigRequest.targetUrl());
        merchantWebhookConfig.setEventTypes(webhookConfigRequest.eventTypes());
        merchantWebhookConfig = webhookConfigRepository.save(merchantWebhookConfig);
        return webhookConfigMapper.toResponse(merchantWebhookConfig, null);
    }

    /**
     * Deletes only the row matching both merchant and configuration IDs.
     */
    @Override
    @Transactional
    public void delete(UUID merchantId, UUID webhookConfigId) {
        webhookConfigRepository.deleteByMerchantIdAndId(merchantId, webhookConfigId);
    }

    /**
     * Enforces tenant scoping for reads and updates, mapping both missing and
     * foreign configurations to not found.
     */
    private MerchantWebhookConfig fetchWebhookConfig(UUID merchantId, UUID webhookConfigId) {
        return webhookConfigRepository
                .findByMerchantIdAndId(merchantId, webhookConfigId).orElseThrow(
                        () -> new ResourceNotFoundException("MERCHANT",
                                "Merchant not found, merchantId: " + merchantId));
    }

}
