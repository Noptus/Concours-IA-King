import java.util.ArrayList;
import java.util.Random;
import grooptown.ia.PlayerConnector;
import grooptown.ia.model.AvailableMoves;
import grooptown.ia.model.Game;
import grooptown.ia.model.GameState;
import grooptown.ia.model.Kingdom;
import grooptown.ia.model.Move;

public class Main {

	public static void main(String[] args) throws Exception {

		// Parametres de la partie
		PlayerConnector.baseUrl = "https://domi-nation.grooptown.com";
		int playerCount = 2;
		int maxTurnToPlay = 28;

		// Creation de la partie
		PlayerConnector[] playerConnectors = new PlayerConnector[playerCount];
		Game newGame = PlayerConnector.createNewGame(playerCount);

		// Creation des joueurs
		for (int i = 0; i < playerConnectors.length; i++) {
			playerConnectors[i] = new PlayerConnector(newGame.getUuid());
			playerConnectors[i].joinGame("Chevre " + i);
		}

		// Creation des parametres et de l'IA
		Parametres p = new Parametres(0, 2, false, true, true, false);
		IA chevre = new IA(p);

		// Pour les 4 premiers moves = choisir dominos avec max couronnes
		for (int i = 0; i < 4; i++) {

			GameState gameState = PlayerConnector.getGameState(newGame.getUuid());
			PlayerConnector playerConnector = getCurrentPlayerConnector(playerConnectors, gameState);
			AvailableMoves m = playerConnector.getAvailableMove();
			Move[] moves = m.getMoves();

			int Maxcouronnes = 0;
			int MoveToMake = 0;
			for (int j = 0; j < moves.length; j++) {
				if (moves[j].getChosenDomino().getTile1().getCrowns() > Maxcouronnes) {
					Maxcouronnes = moves[j].getChosenDomino().getTile1().getCrowns();
					MoveToMake = j;
				}
				if (moves[j].getChosenDomino().getTile2().getCrowns() > Maxcouronnes) {
					Maxcouronnes = moves[j].getChosenDomino().getTile2().getCrowns();
					MoveToMake = j;
				}
			}
			playerConnector.playMove(MoveToMake);
			System.out.println("Played Move : " + MoveToMake);
			System.out.println("Contains : " + Maxcouronnes);

		}

		// Pour tous les moves restants
		// Compteur de fois que le move chevre a ete selectionne
		int Counter = 0;

		for (int i = 4; i < maxTurnToPlay; i++) {

			Thread.sleep(1000);

			System.out.println("");
			System.out.println("TURN : " + i);
			String Player = playerConnectors[maxTurnToPlay % 2].getPlayer().getName();
			System.out.println("Player : " + Player);
			System.out.println("");

			// on recupere l'etat de la partie et on recree le plateau et les pioches
			GameState gameState = PlayerConnector.getGameState(newGame.getUuid());
			PlayerConnector playerConnector = getCurrentPlayerConnector(playerConnectors, gameState);
			Kingdom my_kingdom = new Kingdom();
			for (Kingdom kingdom : gameState.getKingdoms()) // on recupere le plateau du joueur
			{
				if (kingdom.getPlayer().getName().equals(playerConnector.getPlayer().getName()))
					my_kingdom = kingdom;
			}
			Plateau plateau = new Plateau(my_kingdom, p);
			Pioche manche_actuelle = new Pioche(gameState.getPreviousDraft());
			Pioche manche_suivante = new Pioche(gameState.getCurrentDraft());

			plateau.print();
			manche_actuelle.print();
			manche_suivante.print();

			// on appelle notre IA pour qu'elle fasse une action
			chevre.think(plateau, manche_actuelle, manche_suivante, playerConnector.getPlayer().getName().hashCode());

			// on recupere l'action qu'elle a choisit et on la compare avec les moves
			// possible pour envoyer la bonne action

			AvailableMoves m = playerConnector.getAvailableMove();
			Move[] moves = m.getMoves();

			int[] positions = chevre.getPos();
			// Format : position[0],position[1],position[2],position[3]
			// x domino 1 , y domino 1, x domino 2, y domino 2

			int[] domino = new int[5];

			if (i < maxTurnToPlay - 4) {
				domino = chevre.getDomino();
			}
			int[] GoldenMove = new int[9];
			// Les positions que veut l'IA
			try {
				GoldenMove[0] = positions[0];
				GoldenMove[1] = positions[1];
				GoldenMove[2] = positions[2];
				GoldenMove[3] = positions[3];
				// Le domino suivant que veut l'IA
				GoldenMove[4] = domino[4];
			} catch (Exception e) {
			}

			int L = moves.length;
			System.out.println("");
			System.out.println("");
			System.out.println("");
			System.out.println("Nb moves : " + L);

			ArrayList<int[]> ListeComparaison = new ArrayList<int[]>();

			for (int j = 0; j < L; j++) {

				// On tente de récupérer le placement du domino dans ce move.
				int CD1_Col = 0, CD1_Row = 0, CD2_Col = 0, CD2_Row = 0;
				try {
					CD1_Col = moves[j].getPlacedDomino().getTile1Position().getCol();
					CD1_Row = moves[j].getPlacedDomino().getTile1Position().getRow();
					CD2_Col = moves[j].getPlacedDomino().getTile2Position().getCol();
					CD2_Row = moves[j].getPlacedDomino().getTile2Position().getRow();
				} catch (Exception e) {
				}

				// On tente de récupérer le choix du domino suivant.
				int Number = 0;
				try {
					Number = moves[j].getChosenDomino().getNumber();
				} catch (Exception e) {
				}

				int[] MoveOnline = new int[6];
				MoveOnline[0] = CD1_Col;
				MoveOnline[1] = CD1_Row;
				MoveOnline[2] = CD2_Col;
				MoveOnline[3] = CD2_Row;
				MoveOnline[4] = Number;
				MoveOnline[5] = j;
				ListeComparaison.add(MoveOnline);

			}
			System.out.println("");

			Random rand = new Random();
			int GoldenNumber = rand.nextInt(L);

			loop: for (int k = 0; k < L; k++) {
				int[] MoveToCompare = ListeComparaison.get(k);

				System.out.println("MoveToCompare " + k + " :");
				System.out.println(MoveToCompare[0] + " " + MoveToCompare[1] + " " + MoveToCompare[2] + " "
						+ MoveToCompare[3] + " | " + MoveToCompare[4]);
				System.out.println("GoldenMove :");
				System.out.println((GoldenMove[0] - 4) + " " + (GoldenMove[1] - 4) + " " + (GoldenMove[2] - 4) + " "
						+ (GoldenMove[3] - 4) + " | " + GoldenMove[4]);
				System.out.println("");

				if (MoveToCompare[0] == (GoldenMove[0] - 4) && MoveToCompare[1] == (GoldenMove[1] - 4)
						&& MoveToCompare[2] == (GoldenMove[2] - 4) && MoveToCompare[3] == (GoldenMove[3] - 4)
						&& MoveToCompare[4] == (GoldenMove[4] - 4)) {
					GoldenNumber = MoveToCompare[5];
					System.out.println("CHANGED");
					System.out.println("CHANGED");
					System.out.println("CHANGED");
					System.out.println("CHANGED");
					System.out.println("CHANGED");
					System.out.println("CHANGED");
					System.out.println("CHANGED");
					System.out.println("CHANGED");
					System.out.println("Indice used : " + GoldenNumber);
					Counter = Counter + 1;
					System.out.println("Counter : " + Counter);
					break loop;

				}

			}

			ListeComparaison = null;

			playerConnector.playMove(GoldenNumber);

			gameState = PlayerConnector.getGameState(newGame.getUuid());

			System.out.println("");
			System.out.println("ENDED");
			System.out.println("ENDED");
			System.out.println("ENDED");
			System.out.println("ENDED");
			System.out.println("ENDED");
			System.out.println("ENDED");
			System.out.println("ENDED");
			System.out.println("ENDED");
			System.out.println("ENDED");
			System.out.println("ENDED");
			System.out.println("ENDED");

		}

	}

	private static PlayerConnector getCurrentPlayerConnector(PlayerConnector[] playerConnectors, GameState gameState)
			throws Exception {
		for (PlayerConnector playerConnector : playerConnectors) {
			if (gameState.getCurrentPlayer().getName().equals(playerConnector.getPlayer().getName())) {
				return playerConnector;
			}
		}
		throw new Exception("There is no player who should play.");
	}

}