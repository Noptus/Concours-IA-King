import java.util.ArrayList;
import grooptown.ia.model.Kingdom;
import grooptown.ia.model.PlacedTiles;
import grooptown.ia.model.Tile;

import java.util.Arrays;

public class Plateau {
	// CONSTANTES (POUR PLUS DE CLARTE DANS LE CODE)

	// on dÃƒÂ©finit une case vide, sans dominos dessus
	public static final int VIDE = 0;
	public static final int CHATEAU = 7;

	// ATTRIBUTS (PRIVES, ACCESSIBLES UNIQUEMENT DEPUIS CETTE CLASSE)

	// matrice d'entiers qui stockera les cases du plateau avec son contenu
	private int[][] plateau;
	private Parametres p;
	private int XC;
	private int YC;

	// CONSTRUCTEUR
	public Plateau(int ch, Parametres p) {

		this.p = p;
		if (p.modeGrandDuel) {
			plateau = new int[13][13];
			plateau[6][6] = ch;
			XC = 6;
			YC = 6;
		} else {
			plateau = new int[9][9];
			plateau[4][4] = ch;
			XC = 4;
			YC = 4;
		}
		this.placerSingle(4, 4, 17);

	}

	public Plateau(Plateau other) {
		// make a copy of the plateau (/!\ and not a copy of the adress /!\)
		this.plateau = new int[other.sizeX()][other.sizeY()];
		for (int x = 0; x < other.sizeX(); x++) {
			this.plateau[x] = Arrays.copyOf(other.plateau[x], other.sizeY());
		}
		this.p = other.p;
		this.XC = other.XC;
		this.YC = other.YC;
		this.placerSingle(4, 4, 17);

	}

	// CONCOURS IA
	public Plateau(Kingdom kingdom, Parametres p) {
		this.p = p;
		if (p.modeGrandDuel) {
			plateau = new int[13][13];
			plateau[6][6] = 17;
			XC = 6;
			YC = 6;
		} else {
			plateau = new int[9][9];
			plateau[4][4] = 17;
			XC = 4;
			YC = 4;
		}

		for (PlacedTiles tile : kingdom.getPlacedTiles()) {
			plateau[tile.getPosition().getCol() + 4][tile.getPosition().getRow() + 4] = convert(tile.getTile());
		}
		this.placerSingle(4, 4, 17);

	}

	private int convert(Tile tile) {

		int result = 0;
		result += tile.getCrowns() * 10;
		switch (tile.getTerrain()) {
		case "field":
			result += 6;
			break;
		case "forest":
			result += 4;
			break;
		case "water":
			result += 5;
			break;
		case "clay":
			result += 3;
			break;
		case "mine":
			result += 1;
			break;
		case "pasture":
			result += 2;
			break;
		}
		return result;
	}

	// METHODES PUBLIQUES

	// retourne les dimensions du plateau actuel
	public int[] getDimensions() {

		int min_X = 20;
		int max_X = -10;
		int min_Y = 20;
		int max_Y = -10;

		// On avance en augmentant les valeurs, pour trouver les mins
		for (int i = 0; i < sizeX(); i++) {
			for (int j = 0; j < sizeY(); j++) {
				if (getNature(i, j) != 0 && i < min_X) {
					min_X = i;
				}
				if (getNature(i, j) != 0 && j < min_Y) {
					min_Y = j;
				}
			}
		}

		// On fait diminuer les valeurs, pour trouver les max
		for (int i = sizeX() - 1; i >= 0; i--) {
			for (int j = sizeY() - 1; j >= 0; j--) {
				if (getNature(i, j) != 0 && i > max_X) {
					max_X = i;
				}
				if (getNature(i, j) != 0 && j > max_Y) {
					max_Y = j;
				}
			}
		}

		int[] Dimensions = new int[4];
		Dimensions[0] = min_X;
		Dimensions[1] = max_X;
		Dimensions[2] = min_Y;
		Dimensions[3] = max_Y;

		return Dimensions;

	}

	public void placerSingle(int x, int y, int domino) {
		plateau[x][y] = domino;
	}

	// retourne les dimensions ou le joueur peut placer ses dominos
	public int[] getPossibleDimensions() {

		int[] Dim_Actuelles = getDimensions();
		int min_X = Dim_Actuelles[0];
		int max_X = Dim_Actuelles[1];
		int min_Y = Dim_Actuelles[2];
		int max_Y = Dim_Actuelles[3];

		int dim_autorisee;
		if (p.modeGrandDuel == true)
			dim_autorisee = 7;
		else
			dim_autorisee = 5;

		// de combien on peut se decaler sur les cotes en respectant les dimensions
		// autorisees
		int Marge_X = (dim_autorisee - 1) - (max_X - min_X);
		int Marge_Y = (dim_autorisee - 1) - (max_Y - min_Y);

		int Minimum_Ever_X = min_X - Marge_X;
		int Minimum_Ever_Y = min_Y - Marge_Y;
		int Maximum_Ever_X = max_X + Marge_X;
		int Maximum_Ever_Y = max_Y + Marge_Y;

		int[] Dimensions = new int[4];
		Dimensions[0] = Minimum_Ever_X;
		Dimensions[1] = Maximum_Ever_X;
		Dimensions[2] = Minimum_Ever_Y;
		Dimensions[3] = Maximum_Ever_Y;

		return Dimensions;
	}

	public int getTotalCrowns() {
		int Crowns = 0;
		int[] Dimensions = getDimensions();
		for (int i = Dimensions[0]; i < Dimensions[1]; i++) {
			for (int j = Dimensions[2]; j < Dimensions[3]; j++) {
				if (getCouronne(i, j) > 0) {
					Crowns = Crowns + getCouronne(i, j);
				}
			}

		}

		return Crowns;
	}

	// retourne la nature de la case
	public int getNature(int x, int y) {
		// la nature de la case correspond aux unites
		try {
			return plateau[x][y] % 10;
		} catch (Exception e) {
			return -1;
		}

	}

	// retourne true si il y a possibilite de placer le domino dans le plateau
	public boolean isPossible(int[] Domino) {

		int[] PossibleDimensions = getPossibleDimensions();
		for (int x = PossibleDimensions[0]; x <= PossibleDimensions[1]; x++) {
			for (int y = PossibleDimensions[2]; y <= PossibleDimensions[3]; y++) {
				if (getPossibilityHere(x, y, Domino) == true) {
					return true;
				}
			}

		}

		return false;
	}

	// teste s'il est possible de placer le domino ici
	public boolean getPossibilityHere(int x, int y, int[] Domino) {

		if (placementValide(x - 1, y, x, y, Domino) || placementValide(x + 1, y, x, y, Domino)
				|| placementValide(x, y - 1, x, y, Domino) || placementValide(x, y + 1, x, y, Domino))
			return true;
		return false;

	}

	// Chateau au centre > 10 points
	private boolean isEmpire() {
		if (p.modeEmpireMilieu == false) // le mode n'est pas active
			return false;
		int[] Dimensions = getDimensions();

		// distance du centre par rapport au bord de son plateau
		int distance;
		if (p.modeGrandDuel == true)
			distance = 3;
		else
			distance = 2;

		int Center_Y = Dimensions[3] - distance;
		int Center_X = Dimensions[1] - distance;
		// on verifie que le chateau est au centre de son plateau
		if (getNature(Center_X, Center_Y) == 7) {
			return true;
		} else {
			return false;
		}

	}

	// 5*5 cases > 5 points
	private boolean isHarmony() {
		if (p.modeHarmonie == false) // le mode n'est pas active
			return false;
		int[] Dimensions = getDimensions();

		// on verifie que chaque case est bien remplie
		for (int x = Dimensions[0]; x <= Dimensions[1]; x++) {
			for (int y = Dimensions[2]; y <= Dimensions[3]; y++) {
				if (getNature(x, y) == 0) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean canEmpire() {
		// on recupere les dimensions possibles de placement
		int[] PossibleDimensions = getPossibleDimensions();

		// distance du centre par rapport au bord de son plateau
		int distance;
		if (p.modeGrandDuel == true)
			distance = 3;
		else
			distance = 2;

		if (PossibleDimensions[0] <= XC - distance && PossibleDimensions[1] >= XC + distance // les dimensions
																								// permettent toujours
																								// de faire un empire
				&& PossibleDimensions[2] <= YC - distance && PossibleDimensions[3] >= YC + distance)
			return true;
		return false;
	}

	public boolean canHarmony() {
		int[] PossibleDimensions = getPossibleDimensions();
		int[] Dimensions = getDimensions();

		int dim_autorisee;
		if (p.modeGrandDuel == true)
			dim_autorisee = 7;
		else
			dim_autorisee = 5;

		// on inspecte chaque plateau de 7x7 ou de 5x5 qui contient notre plateau
		// existant
		for (int x_beg = PossibleDimensions[0]; x_beg <= Dimensions[0]; x_beg++) {
			for (int y_beg = PossibleDimensions[2]; y_beg <= Dimensions[2]; y_beg++) {
				int[] OnePossibleDimension = new int[] { x_beg, x_beg + (dim_autorisee - 1), y_beg,
						y_beg + (dim_autorisee - 1) };
				if (canHarmonyLikeThat(OnePossibleDimension)) // une combinaison est possible -> retourne vrai
					return true;
			}
		}
		// aucune combinaison n'est possible -> retourne false
		return false;
	}

	public boolean canHarmonyLikeThat(int[] OnePossibleDimension) {
		// on verifie que chaque case libre est bien par groupe pair
		ArrayList<int[]> globallyCounted = new ArrayList<int[]>();
		for (int x = OnePossibleDimension[0]; x <= OnePossibleDimension[1]; x++) {
			for (int y = OnePossibleDimension[2]; y <= OnePossibleDimension[3]; y++) {
				if (isAvailable(x, y)) // case vide -> on verifie qu'elle n'est pas toute seule
				{
					if (count_available(globallyCounted, OnePossibleDimension, x, y) % 2 != 0) // le nombre de case vide
																								// n'est pas pair ! ->
																								// on ne peut pas
																								// remplir la zone
						return false;
				}
			}
		}
		return true;
	}

	// retourne le nombre de cases vides a proximite (la case initiale doit etre
	// vide)
	private int count_available(ArrayList<int[]> globallyCounted, int[] OnePossibleDimension, int x, int y) {
		// hors du plateau POSSIBLE -> on s'arrete la
		if (x < OnePossibleDimension[0] || y < OnePossibleDimension[2] || x > OnePossibleDimension[1]
				|| y > OnePossibleDimension[3])
			return 0;

		// pas disponible -> on s'arrete la
		if (!isAvailable(x, y))
			return 0;

		// on verifie si la case a ete comptee auparavant -> on s'arrete la
		int[] currentPos = { x, y };
		for (int[] pos : globallyCounted) {
			if (pos[0] == currentPos[0] && pos[1] == currentPos[1])
				return 0;
		}

		// on ajoute la case aÂ  l'historique des cases comptees
		globallyCounted.add(currentPos.clone());

		// on inspecte les 4 cases voisines
		return 1 + count_available(globallyCounted, OnePossibleDimension, x - 1, y)
				+ count_available(globallyCounted, OnePossibleDimension, x, y - 1)
				+ count_available(globallyCounted, OnePossibleDimension, x + 1, y)
				+ count_available(globallyCounted, OnePossibleDimension, x, y + 1);
	}

	// retourne le nb de couronnes sur cette case
	public int getCouronne(int x, int y) {
		try {
			// le nombre de couronne correpond aux dizaines
			return plateau[x][y] / 10;
		} catch (Exception e) {
			return 0;
		}
	}

	// retourne le score du plateau
	public int getScore(boolean partieFinie) {
		int score = 0;

		if (partieFinie) {
			if (isHarmony()) {
				score = score + 5;
			}
			if (isEmpire()) {
				score = score + 10;
			}
		}

		// liste qui stocke l'ensemble des cases qui ont ete comptees (pour ne pas les
		// recompter 2 fois quand on inspecte la case suivante)
		ArrayList<int[]> globallyCounted = new ArrayList<int[]>();

		// liste qui stocke les cases voisines qui sont semblables a celle inspectee
		// actuellement
		ArrayList<int[]> currentlyCounted = new ArrayList<int[]>();

		// on cherche pour chaque case les cases voisines semblables
		for (int x = 0; x < sizeX(); x++) {
			for (int y = 0; y < sizeY(); y++) {
				// on compte uniquement si la case n'est pas vide ou n'est pas un chateau
				if (getNature(x, y) != VIDE && getNature(x, y) != CHATEAU) {
					// on vide l'historique des cases voisines a la precedente
					currentlyCounted.clear();

					// on compte les cases voisines semblables aÂ  celle a la position (x, y) et
					// on
					// les sauvegarde pour les inspecter apres
					int cases = count(globallyCounted, currentlyCounted, getNature(x, y), x, y);

					// on compte les couronnes en parcourant la liste des cases voisines semblables
					int couronnes = 0;
					for (int[] position : currentlyCounted) {
						couronnes += getCouronne(position[0], position[1]);
					}

					// le score est calcule commme le produit des couronnes et des cases
					score += couronnes * cases;
				}
			}
		}

		return score;
	}

	// affiche le plateau
	public void print() {
		System.out.println("  |0 |1 |2 |3 |4 |5 |6 | 7| 8");
		for (int y = 0; y < sizeX(); y++) {
			System.out.print(y + " |");
			for (int x = 0; x < sizeY(); x++) {
				System.out.print(getCouronne(x, y));
				System.out.print(getNature(x, y));
				System.out.print("|");
			}
			System.out.println("");
		}
	}

	// retourne le nombre de cases a proximite de meme nature
	// peut egalement fournir la liste des cases dans currentlyCounted, sinon passer
	// null en argument
	public int count(ArrayList<int[]> globallyCounted, ArrayList<int[]> currentlyCounted, int nature, int x, int y) {
		// hors du plateau -> on s'arrete la
		if (x < 0 || y < 0 || x >= sizeX() || y >= sizeY())
			return 0;

		// pas la meme nature -> on s'arrete la
		if (getNature(x, y) != nature)
			return 0;

		// on verifie si la case a ete comptee auparavant -> on s'arrete la
		int[] currentPos = { x, y };
		for (int[] pos : globallyCounted) {
			if (pos[0] == currentPos[0] && pos[1] == currentPos[1])
				return 0;
		}

		// on ajoute la case aÂ l'historique des cases comptees
		globallyCounted.add(currentPos.clone());
		if (currentlyCounted != null)
			currentlyCounted.add(currentPos.clone());

		// on ajoute 1 au compte et on inspecte les 4 cases voisines
		return 1 + count(globallyCounted, currentlyCounted, nature, x - 1, y)
				+ count(globallyCounted, currentlyCounted, nature, x, y - 1)
				+ count(globallyCounted, currentlyCounted, nature, x + 1, y)
				+ count(globallyCounted, currentlyCounted, nature, x, y + 1);
	}

	// place le domino a l'endroit indique (/!\ attention, ne verifie pas que le
	// placement est valide /!\)
	public void placer(int x, int y, int x2, int y2, int[] domino) {

		plateau[x][y] = domino[0] * 10 + domino[1];
		plateau[x2][y2] = domino[2] * 10 + domino[3];
	}

	// rend une case du plateau vide
	public void remove(int x, int y, int x2, int y2) {
		plateau[x][y] = VIDE;
		plateau[x2][y2] = VIDE;
	}

	// retourne si le domino peut etre place a ces coordonnees
	public boolean placementValide(int x, int y, int x2, int y2, int[] domino) {

		if (Math.sqrt(Math.pow(x - x2, 2) + Math.pow(y - y2, 2)) != 1) // les deux morceaux sont separes de plus d'une
																		// case ou sont superposes
			return false;
		if (isAvailable(x, y) && isAvailable(x2, y2)
				&& (isCompatible(x, y, domino[1]) || isCompatible(x2, y2, domino[3]))) { // les cases sont libres et les
																							// dominos compatibles

			// on place le domino
			plateau[x][y] = domino[0] * 10 + domino[1];
			plateau[x2][y2] = domino[2] * 10 + domino[3];

			int[] dimensions = getDimensions();
			int dim_autorisee;
			if (p.modeGrandDuel == true)
				dim_autorisee = 7;
			else
				dim_autorisee = 5;
			// on verifie que le nouveau plateau ne depasse pas les dimensions autorisees
			if (Math.abs(dimensions[0] - dimensions[1]) + 1 > dim_autorisee
					|| Math.abs(dimensions[2] - dimensions[3]) + 1 > dim_autorisee) {
				plateau[x][y] = VIDE;
				plateau[x2][y2] = VIDE;
				return false;
			}
			plateau[x][y] = VIDE;
			plateau[x2][y2] = VIDE;
			return true;
		}
		return false;
	}

	private boolean isAvailable(int x, int y) {
		return getNature(x, y) == VIDE;
	}

	private boolean isCompatible(int x, int y, int nature) {
		boolean isCompatible = false;
		if ((x == XC - 1 && y == YC) || (x == XC + 1 && y == YC) || (x == XC && y == YC - 1)
				|| (x == XC && y == YC + 1)) // rainbow
			isCompatible = true;
		if (getNature(x - 1, y) == nature)
			isCompatible = true;
		if (getNature(x + 1, y) == nature)
			isCompatible = true;
		if (getNature(x, y - 1) == nature)
			isCompatible = true;
		if (getNature(x, y + 1) == nature)
			isCompatible = true;
		return isCompatible;
	}

	public int sizeX() {
		return plateau.length;
	}

	public int sizeY() {
		return plateau[0].length;
	}

}
