package com.waffor.orderservice.controller;

import com.waffor.orderservice.entity.Offer;
import com.waffor.orderservice.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@Slf4j
public class OfferController {

    private final OfferRepository offerRepository;

    @GetMapping
    public ResponseEntity<List<Offer>> getActiveOffers() {
        log.info("REST request to fetch active offers");
        List<Offer> offers = offerRepository.findByActive(true);
        return ResponseEntity.ok(offers);
    }

    @PostMapping
    public ResponseEntity<Offer> createOffer(@RequestBody Offer offer) {
        log.info("REST request to create a new offer: {}", offer.getText());
        // Deactivate all previous active offers
        List<Offer> activeOffers = offerRepository.findByActive(true);
        for (Offer activeOffer : activeOffers) {
            activeOffer.setActive(false);
            offerRepository.save(activeOffer);
        }
        
        offer.setActive(true);
        Offer saved = offerRepository.save(offer);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        log.info("REST request to delete offer: {}", id);
        offerRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
