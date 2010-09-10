package de.fhtrier.gdig.demos.jumpnrun.common.gamelogic.player.states;

import org.newdawn.slick.SlickException;

import de.fhtrier.gdig.demos.jumpnrun.common.gamelogic.player.Player;
import de.fhtrier.gdig.demos.jumpnrun.identifiers.Assets;
import de.fhtrier.gdig.demos.jumpnrun.identifiers.EntityOrder;
import de.fhtrier.gdig.engine.gamelogic.Entity;
import de.fhtrier.gdig.engine.management.Factory;

public class PlayerStandingState extends PlayerAssetState {

	public PlayerStandingState(Player player, Factory factory)
			throws SlickException {
		super(player, Assets.PlayerStandingAnimId, Assets.PlayerStandingAnimImagePath, EntityOrder.Player, factory);
	}

	@Override
	public void enter() {
		getPlayer().getAcc()[Entity.X] = 0.0f;
	}

	@Override
	public void leave() {
	}

	@Override
	public void update() {

	}
}