package com.utn.pokemontcg.game.application.service;

import com.utn.pokemontcg.game.domain.model.PlayerSetupState;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class SetupEngineService {

    private final SecureRandom random = new SecureRandom();

    public PlayerSetupState preparePlayer(int deckSize, int basicCount, int opponentMulligans) {
        PlayerSetupState state = new PlayerSetupState();

        int mulligans = 0;
        while (!drawContainsBasic(deckSize, basicCount, 7)) {
            mulligans++;
        }

        state.setMulligans(mulligans);
        state.setHasActive(true);
        state.setBenchCount(random.nextInt(6));
        state.setPrizeCount(6);
        state.setHandSize(7 + opponentMulligans);
        return state;
    }

    private boolean drawContainsBasic(int deckSize, int basicCount, int drawCount) {
        int successes = 0;
        for (int i = 0; i < drawCount; i++) {
            int remainingDeck = deckSize - i;
            int remainingBasics = basicCount - successes;
            if (remainingBasics <= 0) {
                continue;
            }
            double chance = (double) remainingBasics / remainingDeck;
            if (random.nextDouble() < chance) {
                successes++;
            }
        }
        return successes > 0;
    }
}
