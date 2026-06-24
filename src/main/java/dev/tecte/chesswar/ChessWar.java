package dev.tecte.chesswar;

import dev.tecte.chesswar.admin.AdminCommand;
import dev.tecte.chesswar.board.BoardBlockListener;
import dev.tecte.chesswar.board.BoardEnvironmentPresenter;
import dev.tecte.chesswar.board.BoardManager;
import dev.tecte.chesswar.board.BoardComponent;
import dev.tecte.chesswar.board.BoardVisualManager;
import dev.tecte.chesswar.board.MoveValidator;
import dev.tecte.chesswar.economy.EconomyManager;
import dev.tecte.chesswar.economy.EconomyState;
import dev.tecte.chesswar.economy.ShopController;
import dev.tecte.chesswar.game.CombatPolicy;
import dev.tecte.chesswar.game.component.GameContext;
import dev.tecte.chesswar.game.listener.EnvironmentListener;
import dev.tecte.chesswar.game.listener.GamePieceLifecycleListener;
import dev.tecte.chesswar.game.listener.GameReadyListener;
import dev.tecte.chesswar.game.listener.GameSelectionListener;
import dev.tecte.chesswar.game.listener.GameStartListener;
import dev.tecte.chesswar.game.listener.PlayerJoinListener;
import dev.tecte.chesswar.game.listener.ScoreboardUpdateListener;
import dev.tecte.chesswar.game.manager.CombatManager;
import dev.tecte.chesswar.game.manager.EnvironmentManager;
import dev.tecte.chesswar.game.manager.GameAnnouncer;
import dev.tecte.chesswar.game.manager.GameManager;
import dev.tecte.chesswar.game.manager.PlayerInventoryAdapter;
import dev.tecte.chesswar.game.manager.ScoreboardManager;
import dev.tecte.chesswar.game.manager.TimerManager;
import dev.tecte.chesswar.piece.MythicPieceListener;
import dev.tecte.chesswar.piece.PieceBootstrap;
import dev.tecte.chesswar.piece.PieceCommand;
import dev.tecte.chesswar.piece.PieceDamageListener;
import dev.tecte.chesswar.piece.PieceInteractListener;
import dev.tecte.chesswar.piece.PieceManager;
import dev.tecte.chesswar.piece.PiecePdcMapper;
import dev.tecte.chesswar.piece.PieceState;
import dev.tecte.chesswar.piece.PieceVisualManager;
import io.lumine.mythic.api.MythicProvider;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Slf4j(topic = "ChessWar")
public final class ChessWar extends JavaPlugin {

    private BoardComponent boardComponent;
    private BoardEnvironmentPresenter boardEnvPresenter;
    private BoardManager boardManager;
    private PieceManager pieceManager;
    private GameManager gameManager;
    private TimerManager timerManager;
    private EnvironmentManager environmentManager;
    private ScoreboardManager scoreboardManager;
    private CombatManager combatManager;
    private EconomyManager economyManager;
    private ShopController shopController;
    private GameAnnouncer gameAnnouncer;
    private MoveValidator moveValidator;
    private CombatPolicy combatPolicy;
    private BoardVisualManager boardVisualManager;
    private PieceVisualManager pieceVisualManager;
    private PiecePdcMapper piecePdcMapper;

    @Override
    public void onEnable() {
        initializeInfrastructure();
        initializeManagers();
        registerListeners();
        registerCommands();

        log.info("ChessWar has been enabled!");
    }

    private void initializeInfrastructure() {
        new PieceBootstrap(this).setupMythicMobs();
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.stopHeartbeat();
        }

        log.info("ChessWar has been disabled!");
    }

    private void initializeManagers() {
        final GameContext context = GameContext.create();
        final PieceState pieceState = PieceState.create();
        final BoardComponent boardComponent = BoardComponent.create();
        final EconomyState economyState = EconomyState.create();
        piecePdcMapper = PiecePdcMapper.create(this);

        boardComponent = BoardComponent.create();
        boardEnvPresenter = new BoardEnvironmentPresenter(
                new NamespacedKey(this, BoardEnvironmentPresenter.READY_BUTTON_KEY),
                new NamespacedKey(this, BoardEnvironmentPresenter.TURN_ORDER_KEY),
                boardComponent
        );
        boardManager = new BoardManager(boardComponent, boardEnvPresenter);
        moveValidator = new MoveValidator();
        combatPolicy = new CombatPolicy();
        gameAnnouncer = new GameAnnouncer(context, pieceState);
        pieceVisualManager = new PieceVisualManager();

        pieceManager = new PieceManager(this, pieceState, MythicProvider.get().getMobManager(), piecePdcMapper, boardComponent, boardManager, moveValidator, combatPolicy, gameAnnouncer, pieceVisualManager);
        environmentManager = new EnvironmentManager();

        boardVisualManager = new BoardVisualManager(this, boardComponent, boardManager, gameAnnouncer);
        final PlayerInventoryAdapter inventoryAdapter = new PlayerInventoryAdapter(this, BoardEnvironmentPresenter.TURN_ORDER_KEY);

        timerManager = new TimerManager(context);
        scoreboardManager = new ScoreboardManager(context, inventoryAdapter, economyState, pieceState);

        economyManager = new EconomyManager(context, economyState, gameAnnouncer, scoreboardManager);
        combatManager = new CombatManager(context, boardComponent, boardManager, pieceManager, pieceState, moveValidator, combatPolicy, economyManager, gameAnnouncer);
        shopController = new ShopController(context, economyManager, combatManager, pieceState, gameAnnouncer);

        gameManager = new GameManager(
                this,
                context,
                boardComponent,
                boardEnvPresenter,
                boardManager,
                pieceManager,
                pieceState,
                piecePdcMapper,
                timerManager,
                environmentManager,
                boardVisualManager,
                moveValidator,
                combatManager,
                scoreboardManager,
                economyManager,
                shopController,
                inventoryAdapter,
                gameAnnouncer
        );

        if (boardComponent.hasBoard()) {
            environmentManager.configure(boardComponent.board().origin().getWorld());
        }

        gameManager.loadConfig();
        gameManager.startHeartbeat();
    }

    private void registerListeners() {
        final PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(gameManager, this);
        pluginManager.registerEvents(combatManager, this);
        pluginManager.registerEvents(new GamePieceLifecycleListener(gameManager), this);
        pluginManager.registerEvents(new GameSelectionListener(gameManager), this);
        pluginManager.registerEvents(new GameReadyListener(this, gameManager, boardEnvPresenter), this);
        pluginManager.registerEvents(new ScoreboardUpdateListener(scoreboardManager), this);
        pluginManager.registerEvents(new PlayerJoinListener(gameManager), this);
        pluginManager.registerEvents(new GameStartListener(gameManager), this);
        pluginManager.registerEvents(new MythicPieceListener(pieceManager), this);
        pluginManager.registerEvents(new BoardBlockListener(gameManager, boardEnvPresenter), this);
        pluginManager.registerEvents(new PieceInteractListener(gameManager, gameAnnouncer), this);
        pluginManager.registerEvents(new PieceDamageListener(gameManager, combatManager, pieceManager, piecePdcMapper), this);
        pluginManager.registerEvents(new EnvironmentListener(gameManager), this);
    }

    private void registerCommands() {
        final co.aikar.commands.PaperCommandManager commandManager = new co.aikar.commands.PaperCommandManager(this);

        commandManager.registerCommand(new PieceCommand(gameManager));
        commandManager.registerCommand(new AdminCommand(gameManager, gameAnnouncer));
    }
}
