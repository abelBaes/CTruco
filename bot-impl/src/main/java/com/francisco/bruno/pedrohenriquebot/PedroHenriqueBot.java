package com.francisco.bruno.pedrohenriquebot;

import com.bueno.spi.model.CardRank;
import com.bueno.spi.model.CardToPlay;
import com.bueno.spi.model.GameIntel;
import com.bueno.spi.model.TrucoCard;
import com.bueno.spi.service.BotServiceProvider;

import java.util.ArrayList;
import java.util.List;

public class PedroHenriqueBot implements BotServiceProvider {
    @Override
    public String getName() {
        return "PedroHenrique";
    }

    @Override
    public boolean getMaoDeOnzeResponse(GameIntel intel) {
        int manilhas = countManilhas(intel);
        int highCards = countHighCards(intel);

        return (manilhas >= 2 || (manilhas == 1 && highCards >= 2) || highCards == 3);
    }

    @Override
    public boolean decideIfRaises(GameIntel intel) {
        int opScore= intel.getOpponentScore();
        int score = intel.getScore();
        if ((opScore - score) >= 8) return true;

        if (countHighCards(intel) == 1 && countManilhas(intel) == 1) return true;

        return (handStrengthAverage(intel) >= 9 );
    }

    @Override
    public CardToPlay chooseCard(GameIntel intel) {
        int roundNumber = intel.getRoundResults().size();

        return switch (roundNumber) {
            case 0 -> chooseCardFirstRound(intel);
            case 1 -> chooseCardSecondRound(intel);
            default -> chooseCardThirdRound(intel);
        };
    }

    @Override
    public int getRaiseResponse(GameIntel intel) {
        return 1;
    }

    private CardToPlay chooseCardFirstRound(GameIntel intel) {
        List<TrucoCard> sortedCards = sortCardsByStrength(intel.getCards(), intel.getVira());

        if (intel.getOpponentCard().isEmpty()) {
            return CardToPlay.of(sortedCards.get(1));
        } else {
            return CardToPlay.of(sortedCards.get(1));
        }
    }

    private CardToPlay chooseCardSecondRound(GameIntel intel) {
        List<TrucoCard> sortedCards = sortCardsByStrength(intel.getCards(), intel.getVira());
        return CardToPlay.of(sortedCards.get(0));
    }

    private CardToPlay chooseCardThirdRound(GameIntel intel) {
        List<TrucoCard> sortedCards = sortCardsByStrength(intel.getCards(), intel.getVira());
        return CardToPlay.of(sortedCards.get(0));
    }

    private List<TrucoCard> sortCardsByStrength(List<TrucoCard> cards, TrucoCard vira) {
        List<TrucoCard> sortedCards = new ArrayList<>(cards);
        sortedCards.sort((card1, card2) -> Integer.compare(card2.relativeValue(vira), card1.relativeValue(vira)));
        return sortedCards;
    }

    public int countManilhas(GameIntel intel){
        return (int) intel.getCards().stream().filter(card -> card.isManilha(intel.getVira())).count();
    }

    private int countHighCards(GameIntel intel) {
        TrucoCard vira = intel.getVira();
        return (int) intel.getCards().stream()
                .filter(card -> !card.isManilha(vira))
                .filter(card -> card.getRank() == CardRank.THREE ||
                        card.getRank() == CardRank.TWO ||
                        card.getRank() == CardRank.ACE)
                .count();
    }

    private double handStrengthAverage(GameIntel intel) {
        TrucoCard vira = intel.getVira();
        return intel.getCards().stream()
                .mapToInt(card -> card.relativeValue(vira))
                .average()
                .orElse(0);
    }
}
