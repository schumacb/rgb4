package de.fhtrier.gdig.demos.jumpnrun.common;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import de.fhtrier.gdig.demos.jumpnrun.client.network.protocol.QueryAction;
import de.fhtrier.gdig.demos.jumpnrun.common.Constants.GamePlayConstants;
import de.fhtrier.gdig.demos.jumpnrun.common.entities.physics.CollisionManager;
import de.fhtrier.gdig.demos.jumpnrun.common.entities.physics.LevelCollidableEntity;
import de.fhtrier.gdig.demos.jumpnrun.common.network.NetworkData;
import de.fhtrier.gdig.demos.jumpnrun.common.network.PlayerData;
import de.fhtrier.gdig.demos.jumpnrun.identifiers.Assets;
import de.fhtrier.gdig.demos.jumpnrun.identifiers.EntityOrder;
import de.fhtrier.gdig.demos.jumpnrun.identifiers.PlayerState;
import de.fhtrier.gdig.engine.entities.Entity;
import de.fhtrier.gdig.engine.entities.gfx.AnimationEntity;
import de.fhtrier.gdig.engine.entities.gfx.ImageEntity;
import de.fhtrier.gdig.engine.management.AssetMgr;
import de.fhtrier.gdig.engine.management.Factory;
import de.fhtrier.gdig.engine.network.NetworkComponent;

public class Player extends LevelCollidableEntity {

	// HACK make it private
	public int currentState = -1;
	private final ImageEntity idleImage;
	private final Entity playerGroup;
	private final AnimationEntity runAnimation;
	private final AnimationEntity jumpAnimation;
	private final Animation jump;

	private final float maxPlayerSpeed = 1000.0f;
	private final float playerHalfWidth = 48;

	public PlayerState state;

	public Player(final int id, final Factory factory) throws SlickException {
		super(id);
		state = new PlayerState();
		state.name = "Player";
		state.color = Constants.StateColor.RED; // player gets default-color:
												// red
		state.weaponColor = Constants.StateColor.RED; // weapon of player get
														// default-color: red
		AssetMgr assets = factory.getAssetMgr();

		// gfx
		assets.storeImage(Assets.PlayerIdleImage, "sprites/player/Idle.png");
		assets.storeAnimation(Assets.PlayerRunAnim, "sprites/player/Run.png",
				96, 96, 75);
		this.jump = assets.storeAnimation(Assets.PlayerJumpAnim,
				"sprites/player/Jump.png", 96, 96, 70);
		this.jump.setLooping(false);

		this.idleImage = factory.createImageEntity(Assets.PlayerIdleImage,
				Assets.PlayerIdleImage);
		this.runAnimation = factory.createAnimationEntity(Assets.PlayerRunAnim,
				Assets.PlayerRunAnim);
		this.jumpAnimation = factory.createAnimationEntity(
				Assets.PlayerJumpAnim, Assets.PlayerJumpAnim);

		this.playerGroup = factory.createEntity(EntityOrder.Player);

		this.playerGroup.getData()[Entity.CENTER_X] = 48;
		this.playerGroup.getData()[Entity.CENTER_Y] = 48;

		this.playerGroup.add(this.idleImage);
		this.playerGroup.add(this.runAnimation);
		this.playerGroup.add(this.jumpAnimation);

		this.add(this.playerGroup);

		// physics
		// X Y OX OY SY SY ROT
		initData(new float[] { 200, 200, 0, 0, 1, 1, 0 }); // pos +
															// center of
															// rotation +
															// scale +
															// rot
		setVel(new float[] { 0, 0, 0, 0, 0, 0, 0 }); // no speed
		setAcc(new float[] { 0, GamePlayConstants.gravity, 0, 0, 0, 0, 0 }); // gravity
		setBounds(new Rectangle(30, 0, 36, 96)); // bounding box

		setVisible(true);
		// order
		this.setOrder(EntityOrder.Player);

		// startup
		setState(PlayerState.Idle);
	}

	@Override
	public void applyNetworkData(final NetworkData networkData) {
		super.applyNetworkData(networkData);

		if ((this.currentState == PlayerState.Idle)
				&& (Math.abs(getData()[X] - getPrevPos()[X]) < Constants.EPSILON)
				&& (Math.abs(getData()[Y] - getPrevPos()[Y]) < Constants.EPSILON)) {
			getVel()[X] = getVel()[Y] = 0.0f;
		}
		this.setState(((PlayerData) networkData).getState());
	}

	private void enterState(final int state) {
		this.currentState = state;
		switch (state) {
		case PlayerState.Idle:
			this.getAcc()[Entity.X] = 0.0f;
			this.idleImage.setActive(true);
			this.idleImage.setVisible(true);
			break;
		case PlayerState.RunLeft:
			this.getAcc()[Entity.X] = -2000.0f;
			this.playerGroup.getData()[Entity.SCALE_X] = 1;
			this.runAnimation.setActive(true);
			this.runAnimation.setVisible(true);
			break;
		case PlayerState.RunRight:
			this.getAcc()[Entity.X] = 2000.0f;
			this.playerGroup.getData()[Entity.SCALE_X] = -1;
			this.runAnimation.setActive(true);
			this.runAnimation.setVisible(true);
			break;
		case PlayerState.Jump:
			this.getVel()[Entity.Y] = -800;
			this.jump.start();
			this.jumpAnimation.setActive(true);
			this.jumpAnimation.setVisible(true);
			break;
		}
	}

	// Nur zum testen der Kollision
	@Override
	public boolean handleCollisions() {
		markCollisionTiles(12);

		boolean result = super.handleCollisions();

		if (CollisionManager.iColideWith(this).size() != 0) {
			// HACK for debug only
			this.map.setTileId(0, 0, 0, 0);
		} else {
			// HACK for debug only
			this.map.setTileId(0, 0, 0, 13);
		}

		return result;
	}

	// input
	@Override
	public void handleInput(final Input input) {
		if (this.isActive()) {
			if (!input.isKeyDown(Input.KEY_LEFT)
					&& !input.isKeyDown(Input.KEY_RIGHT)
					&& !input.isKeyDown(Input.KEY_SPACE)) {
				this.setState(PlayerState.Idle);
			}

			if (input.isKeyDown(Input.KEY_LEFT)) {
				this.setState(PlayerState.RunLeft);
			}

			if (input.isKeyDown(Input.KEY_RIGHT)) {
				this.setState(PlayerState.RunRight);
			}

			if (input.isKeyDown(Input.KEY_UP)) {
				if (this.isOnGround()) {
					this.setState(PlayerState.Jump);
				}
			}

			if (input.isKeyPressed(Input.KEY_SPACE)) {
				NetworkComponent.getInstance().sendCommand(
						new QueryAction(PlayerAction.DROPGEM));
			}
		}
		super.handleInput(input);
	}

	// network
	@Override
	protected NetworkData _createNetworkData() {
		return new PlayerData(getId());
	}

	@Override
	public NetworkData getNetworkData() {
		PlayerData result = (PlayerData) super.getNetworkData();
		result.state = this.currentState;

		return result;
	}

	private void leaveState(int state) {
		switch (state) {
		case PlayerState.Idle:
			this.idleImage.setActive(false);
			this.idleImage.setVisible(false);
			break;
		case PlayerState.RunLeft:
		case PlayerState.RunRight:
			this.runAnimation.setActive(false);
			this.runAnimation.setVisible(false);
			break;
		case PlayerState.Jump:
			this.jumpAnimation.setActive(false);
			this.jumpAnimation.setVisible(false);
		}
	}

	// render
	@Override
	public void renderImpl(final Graphics g) {

		if (this.getId() == -1) {
			throw new RuntimeException("Wrong Initialization: no Client ID set");
		}

		super.renderImpl(g);

		if (this.state.name != null) {
			float x = playerHalfWidth - g.getFont().getWidth(state.name) / 2.0f;
			float y = -g.getFont().getHeight(state.name);
			g.setColor(Constants.StateColor.constIntoColor(state.color)); // colors
																			// the
																			// name
																			// of
																			// player
																			// with
																			// his
																			// color

			g.drawString(state.name + " " + state.weaponColor, x, y);
			g.setColor(Color.white); // default-color when changed
		}

	}

	public void setLevel(final Level level) {
		this.setMap(level.getMap());
	}

	// game logic
	public void setState(final int state) {
		if (state != this.currentState) {
			this.leaveState(this.currentState);
			this.enterState(state);
		}
	}

	// update
	@Override
	public void update(final int deltaInMillis) {

		if (this.isActive()) {

			super.update(deltaInMillis); // calc physics

			if (this.getVel()[Entity.X] > this.maxPlayerSpeed) {
				this.getVel()[Entity.X] = this.maxPlayerSpeed;
			}

			if (this.getVel()[Entity.Y] > this.maxPlayerSpeed) {
				this.getVel()[Entity.Y] = this.maxPlayerSpeed;
			}

			if (this.getVel()[Entity.X] < -this.maxPlayerSpeed) {
				this.getVel()[Entity.X] = -this.maxPlayerSpeed;
			}

			if (this.getVel()[Entity.Y] < -this.maxPlayerSpeed) {
				this.getVel()[Entity.Y] = -this.maxPlayerSpeed;
			}

			if (this.currentState == PlayerState.Idle
					&& Math.abs(this.getData()[Entity.X]
							- this.getPrevPos()[Entity.X]) < Constants.EPSILON
					&& Math.abs(this.getData()[Entity.Y]
							- this.getPrevPos()[Entity.Y]) < Constants.EPSILON) {
				this.getVel()[Entity.X] = this.getVel()[Entity.Y] = 0.0f;
			}
		}
	}
}
