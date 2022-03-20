/*
 *  Copyright (C) 2022 Lucas B. R. de Oliveira - IFSP/SCL
 *  Contact: lucas <dot> oliveira <at> ifsp <dot> edu <dot> br
 *
 *  This file is part of CTruco (Truco game for didactic purpose).
 *
 *  CTruco is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CTruco is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CTruco.  If not, see <https://www.gnu.org/licenses/>
 */

package com.bueno.domain.usecases.bot;

import com.bueno.domain.entities.game.Game;
import com.bueno.domain.entities.intel.Intel;
import com.bueno.domain.entities.player.Player;
import com.bueno.domain.usecases.game.GameRepository;
import com.bueno.domain.usecases.hand.usecases.PlayCardUseCase;
import com.bueno.domain.usecases.hand.usecases.ScoreProposalUseCase;
import com.bueno.spi.service.BotServiceManager;
import com.bueno.spi.service.BotServiceProvider;

import java.util.List;
import java.util.Objects;

public class BotUseCase {
    private final GameRepository repo;
    private MaoDeOnzeHandler maoDeOnzeHandler;
    private RaiseHandler raiseHandler;
    private CardPlayingHandler cardHandler;
    private RaiseRequestHandler requestHandler;

    public BotUseCase(GameRepository repo) {
        this(repo, null, null, null, null);
    }

    BotUseCase(GameRepository repo, MaoDeOnzeHandler maoDeOnze, RaiseHandler raise, CardPlayingHandler card, RaiseRequestHandler request){
        this.repo = Objects.requireNonNull(repo, "GameRepository must not be null.");
        this.maoDeOnzeHandler = maoDeOnze;
        this.raiseHandler = raise;
        this.cardHandler = card;
        this.requestHandler = request;
    }

    public Intel playWhenNecessary(Game game) {
        final Player currentPlayer = game.currentHand().getCurrentPlayer();
        final Intel intel = game.getIntel();

        if (!isBotTurn(currentPlayer, intel)) return intel;

        initializeNullHandlers(currentPlayer, intel, BotServiceManager.load(currentPlayer.getUsername()));

        if (maoDeOnzeHandler.handle()) return game.getIntel();
        if (raiseHandler.handle()) return game.getIntel();
        if (cardHandler.handle()) return game.getIntel();
        requestHandler.handle();
        return game.getIntel();
    }

    private boolean isBotTurn(Player handPlayer, Intel intel) {
        final var currentPlayerUUID = intel.currentPlayerUuid();
        if (currentPlayerUUID.isEmpty() || intel.isGameDone() || !handPlayer.isBot()) return false;
        return handPlayer.getUuid().equals(currentPlayerUUID.get());
    }

    private void initializeNullHandlers(Player currentPlayer, Intel intel, BotServiceProvider botService) {
        if (maoDeOnzeHandler == null)
            maoDeOnzeHandler = new MaoDeOnzeHandler(new ScoreProposalUseCase(repo), botService, currentPlayer, intel);
        if (raiseHandler == null)
            raiseHandler = new RaiseHandler(new ScoreProposalUseCase(repo), botService, currentPlayer, intel);
        if (cardHandler == null)
            cardHandler = new CardPlayingHandler(new PlayCardUseCase(repo), botService, currentPlayer, intel);
        if (requestHandler == null)
            requestHandler = new RaiseRequestHandler(new ScoreProposalUseCase(repo), botService, currentPlayer, intel);
    }

    public static List<String> availableBots(){
        return BotServiceManager.providersNames();
    }
}
