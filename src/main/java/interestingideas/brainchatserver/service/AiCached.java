package interestingideas.brainchatserver.service;

import interestingideas.brainchatserver.model.AI;
import interestingideas.brainchatserver.repository.AIRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiCached {
    private final AIRepository aiRepository;
    @Cacheable(value = "aiByIdCache", key = "#id")
    public AI findByIdCached(Long id) {
        return aiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
    }

    @CacheEvict(value = "aiByIdCache", key = "#aiId")
    public void deleteCacheAIById(Long aiId) {

    }
}
