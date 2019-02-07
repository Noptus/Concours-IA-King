import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
import java.util.List;

public class IA {

	// structure qui represente un move de l'ia, cad un domino place a une certaine
	// position
	public class Move {
		public int[] pos;
		public int[] domino;
		public boolean impossible;

		public Move(int[] pos, int[] domino) // le domino peut etre place
		{
			this.pos = pos;
			this.domino = domino;
			this.impossible = false;
		}

		public Move(int[] domino) // le domino ne peut pas etre place
		{
			this.domino = domino;
			this.impossible = true;
		}

		public Move(Move other) // on cree une copie d'un move existant
		{
			if (!other.impossible)
				this.pos = other.pos.clone();
			this.domino = other.domino;
			this.impossible = other.impossible;
		}
	}

	private ArrayList<Move> next_moves; // stocke les prochains moves de l'ia

	// stocke l'etat du jeu pour ne pas avoir a le passer en argument des fonctions
	private Parametres p;
	private Pioche pioche_actuelle;
	private Pioche pioche_suivante;
	private int joueur;
	private int manche;

	private int nb_moves;
	private boolean derniere_manche;

	// stocke tous les moves possibles
	private ArrayList<ArrayList<Move>> all_moves = new ArrayList<ArrayList<Move>>();

	public IA(Parametres p) {
		this.p = p;
	}

	public int[] getPos() {
		return next_moves.get(0).pos;
	}

	public int[] getDomino() {

		if (nb_moves == 2)
			return next_moves.get(1).domino;

		return next_moves.get(next_moves.size() - 1).domino;

	}

	public ArrayList<Move> getMoves() {
		return next_moves;
	}

	// fonction principale qui determine les prochains moves de l'ia, a appeler a
	// chaque fois que c'est a elle de jouer (/!\ au debut de chaque tour /!\)
	public void think(Plateau plateau, Pioche pioche_actuelle, Pioche pioche_suivante, int joueur) {
		
		plateau.placerSingle(4, 4, 17);
		long debut = System.currentTimeMillis();

		// stocker les infos de jeu
		this.pioche_actuelle = pioche_actuelle;
		this.pioche_suivante = pioche_suivante;
		this.joueur = joueur;

		if (pioche_suivante.getSize() == 0) // si la pioche suivante est vide, il s'agit du dernier tour, donc on a deux
											// fois moins de moves a anticiper
			this.derniere_manche = true;
		else
			this.derniere_manche = false;

		if (pioche_actuelle.getDomino(joueur) != pioche_actuelle.getSecondDomino(joueur)) // si le joueur a 2 dominos a
																							// placer, il s'agit d'un
																							// mode 1v1 et donc on a
																							// jusqu'a 4 moves a
																							// anticiper
			this.nb_moves = 3; // /!\ 4 -> peut etre tres long a calculer /!\
		else
			this.nb_moves = 2;

		// ETAPE 1 : on recupere la liste de tous les moves possibles
		searchMoves(new Plateau(plateau), new ArrayList<Move>());

		long etape1 = System.currentTimeMillis();
		// System.out.println("etape 1 : " + (etape1 - debut) + " ms");

		// ETAPE 2 : on calcule les gains de chaque move
		List<Float> all_gains = new ArrayList<Float>();
		for (ArrayList<Move> moves : all_moves) {
			// check_moves(new Plateau(plateau), moves);
			all_gains.add(getGain(plateau, moves));
		}

		long etape2 = System.currentTimeMillis();
		// System.out.println("etape 2 : " + (etape2 - etape1) + " ms");

		// ETAPE 3 : on stocke les indices des moves qui ont le meilleur gain (a egalite
		// avec le max)
		float best_gain = Collections.max(all_gains);
		ArrayList<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i < all_gains.size(); i++) {
			if (all_gains.get(i) == best_gain)
				indices.add(i);
		}

		long etape3 = System.currentTimeMillis();
		// System.out.println("etape 3 : " + (etape3 - etape2) + " ms");

		// ETAPE 4 : on en choisit un de maniere random
		Random random = new Random();
		next_moves = all_moves.get(indices.get(random.nextInt(indices.size())));

		long etape4 = System.currentTimeMillis();
		// System.out.println("etape 4 : " + (etape4 - etape3) + " ms");

		// afficher les infos dans la console
		// System.out.println("IA Joueur " + joueur);
		// System.out.println("nombre de moves total : " + all_moves.size());
		// System.out.println("nombre de moves conserves : " + indices.size());
		// System.out.println("moves selectionnes :");
		printMoves(next_moves);
		// System.out.println("gains : " + best_gain);

		all_moves.clear();
	}

	public void printMoves(ArrayList<Move> moves) {
		for (Move move : moves) {
			System.out.println("domino : " + (move.domino[0] * 10 + move.domino[1]) + "-"
					+ (move.domino[2] * 10 + move.domino[3]));
			if (move.impossible)
				System.out.println("-> placement impossible");
			else
				System.out.println("-> aux coordonnees : " + move.pos[0] + "-" + move.pos[1] + " et " + move.pos[2]
						+ "-" + move.pos[3]);
		}
	}

	public void searchMoves(Plateau plateau, ArrayList<Move> current_moves) {
		switch (current_moves.size()) {
		case 0: // premier domino a placer
		{
			iterateMoves(new Plateau(plateau), new ArrayList<Move>(current_moves), pioche_actuelle.getDomino(joueur));
			break;
		}
		case 1: // deuxieme domino a placer ou premier domino a piocher
		{
			if (nb_moves == 2) // premier domino a piocher
			{
				for (int i = 0; i < pioche_suivante.getSize(); i++) {
					if (pioche_suivante.getJoueurByIndex(i) != 0) // le domino n'est pas disponible
						continue;
					iterateMoves(new Plateau(plateau), new ArrayList<Move>(current_moves),
							pioche_suivante.getDominoByIndex(i));
				}
			} else // deuxieme domino a placer
			{
				iterateMoves(new Plateau(plateau), new ArrayList<Move>(current_moves),
						pioche_actuelle.getSecondDomino(joueur));
			}
			break;
		}
		case 2: // premier domino a piocher
		{
			for (int i = 0; i < pioche_suivante.getSize(); i++) {
				if (pioche_suivante.getJoueurByIndex(i) != 0) // le domino n'est pas disponible
					continue;
				iterateMoves(new Plateau(plateau), new ArrayList<Move>(current_moves),
						pioche_suivante.getDominoByIndex(i));
			}
			break;
		}
		case 3: // deuxieme domino a piocher
		{
			for (int i = 0; i < pioche_suivante.getSize(); i++) {
				if (pioche_suivante.getJoueurByIndex(i) != 0
						|| pioche_suivante.getDominoByIndex(i) == current_moves.get(2).domino) // le domino n'est pas
																								// disponible ou a deja
																								// ete choisi au move
																								// d'avant
					continue;
				iterateMoves(new Plateau(plateau), new ArrayList<Move>(current_moves),
						pioche_suivante.getDominoByIndex(i));
			}
			break;
		}
		}
	}

	public void iterateMoves(Plateau plateau, ArrayList<Move> current_moves, int domino[]) {
		ArrayList<Move> moves = getMoves(plateau, domino); // on recupere tous les moves possibles avec ce domino
		for (Move move : moves) // pour chacun d'eux
		{
			ArrayList<Move> current_moves_evolved = new ArrayList<Move>(current_moves);
			current_moves_evolved.add(new Move(move)); // on l'ajoute a notre move actuel
			Plateau plateau_evolved = new Plateau(plateau);

			if (current_moves_evolved.size() == nb_moves / (derniere_manche ? 2 : 1)) // ce move est complet -> on
																						// l'ajoute et on arrete la
			{
				all_moves.add(new ArrayList<Move>(current_moves_evolved));
			} else // sinon on continue de former le move actuel jusqu'a ce qu'il soit complet
			{
				if (!move.impossible) // on place le domino
					plateau_evolved.placer(move.pos[0], move.pos[1], move.pos[2], move.pos[3], move.domino);
				searchMoves(new Plateau(plateau_evolved), new ArrayList<Move>(current_moves_evolved));
			}
		}
	}

	// retourne tous les moves possibles du domino en question
	public ArrayList<Move> getMoves(Plateau plateau, int[] domino) {

		ArrayList<Move> moves = new ArrayList<Move>();
		int[] pos = new int[4];

		/// on essaye chaque case contenue dans les dimensions possibles (= qui
		/// respectent la taille autorisee du plateau)
		int[] PossibleDimensions = plateau.getPossibleDimensions();
		for (int i = PossibleDimensions[0]; i <= PossibleDimensions[1]; i++) {
			for (int j = PossibleDimensions[2]; j <= PossibleDimensions[3]; j++) {
				for (int k = 0; k < 4; k++) { // on teste les 4 orientations du domino possibles
					switch (k) {
					case 0:
						pos[0] = i - 1;
						pos[1] = j;
						pos[2] = i;
						pos[3] = j;
						break;
					case 1:
						pos[0] = i + 1;
						pos[1] = j;
						pos[2] = i;
						pos[3] = j;
						break;
					case 2:
						pos[0] = i;
						pos[1] = j - 1;
						pos[2] = i;
						pos[3] = j;
						break;
					case 3:
						pos[0] = i;
						pos[1] = j + 1;
						pos[2] = i;
						pos[3] = j;
						break;
					}

					if (plateau.placementValide(pos[0], pos[1], pos[2], pos[3], domino)) // si le placement est valide,
																							// on le sauvegarde
						moves.add(new Move(pos.clone(), domino));
				}
			}
		}
		if (moves.size() == 0) // on verifie que le domino peut etre place -> sinon on ajoute un move
								// impossible
			moves.add(new Move(domino));

		return moves;
	}

	// retourne le gain de l'ensemble des moves
	public float getGain(Plateau plateau, ArrayList<Move> moves) {
		// ETAPE 1 : on calcule le score avant de jouer les moves
		int S = plateau.getScore(false);

		// ETAPE 2 : on place les moves temporairement
		for (Move move : moves) {
			if (move.impossible)
				continue;

			plateau.placer(move.pos[0], move.pos[1], move.pos[2], move.pos[3], move.domino);
		}

		// ETAPE 3 : on calcule le score apres les moves et on calcule la difference
		S = plateau.getScore(false) - S; // on calcule le score apres les moves et on retire le score avant pour
											// calculer la difference

		// ETAPE 4 : on regarde si les moves permettent toujours d'avoir les bonus de
		// fin de partie
		boolean canHarmony = false;
		if (p.modeHarmonie) {
			if (manche < 3) // /!\ tres long a calculer, donc on assume pour les premieres manches qu'il est
							// toujours possible de le faire /!\
				canHarmony = true;
			else
				canHarmony = plateau.canHarmony();
		}
		boolean canEmpire = false;
		if (p.modeEmpireMilieu)
			canEmpire = plateau.canEmpire();

		// ETAPE 5 : on calcule le gain
		// permet de determiner le meilleur move /!\ G >= 0 obligatoirement /!\
		// ajuster cette formule permet de modifier le comportement de l'IA
		float G = S + (canHarmony ? 5 : 0) + (canEmpire ? 10 : 0);

		// ETAPE 6 : on supprime les moves places temporairement
		for (Move move : moves) {
			if (move.impossible)
				continue;

			plateau.remove(move.pos[0], move.pos[1], move.pos[2], move.pos[3]);
		}

		// ETAPE 7 : on retourne le gain
		return G;
	}

	// fonction de debug qui verifie que le move propose est possible
	public void check_moves(Plateau plateau, ArrayList<Move> moves) {
		boolean error = false;
		for (Move move : moves) {
			if (move.impossible)
				continue;

			if (plateau.placementValide(move.pos[0], move.pos[1], move.pos[2], move.pos[3], move.domino))
				plateau.placer(move.pos[0], move.pos[1], move.pos[2], move.pos[3], move.domino);
			else {
				error = true;
				break;
			}
		}
		if (error) {
			System.out.println("move incorrect:");
			printMoves(moves);
		}
	}
}
