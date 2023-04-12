package abstraction.eq2Producteur2;

//code écrit par Nathan

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import abstraction.eqXRomu.contratsCadres.ContratCadre;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Variable;

import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Lot;

public class Producteur2AStockeur extends Producteur2Acteur {
	
	private HashMap<Feve, Lot> stocks; // Représente les stocks réels par type de fève,
									   // tout en gardant en mémoire l'étape de production
									   // de chaque fève stocké
	private HashMap<Feve, Variable> stocksTot;// Est composé des indicateurs de stock,
											  // que l'on tiens à jour à chaque modification
											  // des stocks
	
	public Producteur2AStockeur() {
		super();
	}
	
	public void initialiser() {
		super.initialiser();
		
		this.stocks = this.createStocks();
		
		this.stocks.get(Feve.F_BQ).ajouter(0, 10000);
		this.stocks.get(Feve.F_MQ).ajouter(0, 10000);
		this.stocks.get(Feve.F_MQ_BE).ajouter(0, 10000);
		this.stocks.get(Feve.F_HQ_BE).ajouter(0, 10000);
		
		this.stocksTot = new HashMap<Feve, Variable>();
		
		this.stocksTot.put(Feve.F_BQ, stockTotBasse);
		this.stocksTot.put(Feve.F_MQ, stockTotMoy);
		this.stocksTot.put(Feve.F_MQ_BE, stockTotMoyBE);
		this.stocksTot.put(Feve.F_HQ_BE, stockTotHauteBE);
		
		this.majTot();
	}
	
	private HashMap<Feve, Lot> createStocks(){
		HashMap<Feve, Lot> stocks = new HashMap<Feve, Lot>();
		
		stocks.put(Feve.F_BQ, new Lot(Feve.F_BQ));
		stocks.put(Feve.F_MQ, new Lot(Feve.F_MQ));
		stocks.put(Feve.F_MQ_BE, new Lot(Feve.F_MQ_BE));
		stocks.put(Feve.F_HQ_BE, new Lot(Feve.F_HQ_BE));
		
		return stocks;
	}
	
	private HashMap<Feve, Double> createStock(){
		HashMap<Feve, Double> stock = new HashMap<Feve, Double>();
		for (Feve f: this.lesFeves)
			stock.put(f, 0.);
		return stock;
	}
	
	// mets à jour le stock total de feve de type f
	private void majTot(Feve f) {
		this.stocksTot.get(f).setValeur(this, this.stocks.get(f).getQuantiteTotale(), this.cryptogramme);
	}
	
	// mets à jour les stocks totaux
	private void majTot() {
		for (Feve f : this.lesFeves) {
			this.majTot(f);
		}
	}
	
	public void next() {
		super.next();
		this.majPerim();
		this.majTot();
		//System.out.println(this.stocks.get(Feve.F_BQ));
		/*this.ajouterStock(Feve.F_BQ, Filiere.LA_FILIERE.getEtape(), 1000);
		this.retirerStock(Feve.F_BQ, 500);
		Lot lotHQ_BE = new Lot(Feve.F_HQ_BE);
		lotHQ_BE.ajouter(0, 1000);
		this.ajouterStock(lotHQ_BE);
		//this.retirerStock(Feve.F_MQ, 500);
		System.out.println(this.stocksString());
		System.out.println(this.stocksTotString());
		System.out.println(this.getStockTotTime(Feve.F_BQ, 2));
		System.out.println(this.getStockTotStep(Feve.F_BQ, 2));*/
	}
	/**
	 * Mets à jour la périmation du stock de l'acteur
	 */
	private void majPerim() {
		majPerim(this.stocks, Filiere.LA_FILIERE.getEtape());
	}
	
	/**
	 * Effectue la déclasification et la périmation des fèves du stock stocks,
	 * contenant des quatres types de fèves, par rapport à l'étape etape considéré
	 * @param stocks le stock de fèves des différents types
	 * @param etape l'étape considéré pour calculer la périmation
	 */
	private void majPerim(HashMap<Feve, Lot> stocks, int etape) {
		int etapeDegrad = etape - (int)this.tempsDegradationFeve.getValeur();
		int etapePerim = etapeDegrad - (int)this.tempsPerimationFeve.getValeur();
		for (Feve f : this.lesFeves) {
			HashMap<Integer, Double> stock = stocks.get(f).getQuantites();
			if (stock.containsKey(etapeDegrad)) {
				if (f == Feve.F_MQ || f == Feve.F_MQ_BE) {
					stocks.get(Feve.F_BQ).ajouter(etapeDegrad, stock.get(etapeDegrad));
				}
				if (f == Feve.F_HQ_BE) {
					stocks.get(Feve.F_MQ_BE).ajouter(etapeDegrad, stock.get(etapeDegrad));
				}
				stock.remove(etapeDegrad);
			}
			Set<Integer> key = new HashSet<>(stock.keySet());
			for (int i: key) 
				if (i <= etapePerim)
					stock.remove(i);
		}
	}
	
	/**
	 * Renvoie la variable représentant la quantité totale de fèves stockée du type f
	 * @return la variable de la quantité totale de fèves stockée du type f
	 * @param f le type de fève
	 */
	protected Variable getStockTot(Feve f) {
		return this.stocksTot.get(f);
	}
	
	
	/**
	 * Calcule la quantité totale de fève stockées, tout type confondu
	 * @return la quantité totale de fève stockées
	 */
	protected double getStockTotTot() {
		double tot = 0;
		for (Feve f: this.stocksTot.keySet()) 
			tot += this.stocksTot.get(f).getValeur();
		return tot;
	}
	
	/**
	 * Calcule la quantité de fèves stocké du type f produites avant l'étape etape (inclus)
	 * @return la quantité de fève
	 * @param f Le type de fève
	 * @param etape L'étape maximal de production
	 */
	protected double getStockTotStep(Feve f, int etape) {
		HashMap<Integer, Double> stockFeve = this.stocks.get(f).getQuantites();
		double quantiteTot = 0.;
		for(int i: stockFeve.keySet()) 
			if (i <= etape) {
				quantiteTot += stockFeve.get(i);
			}
		return quantiteTot;
	}
	
	/**
	 * Calcule la quantité de fèves stocké du type f, qui sont produite depuis plus de nbStepStocke étapes (nbStepStocke étant inclus)
	 * @return la quantité de fève
	 * @param f le type de fève 
	 * @param nbStepProduite le temps minimal depuis la production
	 */
	protected double getStockTotTime(Feve f, int nbStepProduite) {
		return this.getStockTotStep(f, Filiere.LA_FILIERE.getEtape() - nbStepProduite);
	}
	
	/**
	 * Calcule le coût de stockage du stock actuel pour une étape
	 * @return le coût du stockage
	 */
	protected double coutStockage() {
		return this.coutMoyenStock.getValeur() * this.getStockTotTot();
	}
	
	/**
	 * Renvoie le stock restant théorique à l'étape etape en retirant les contrats cadres et en ajoutant la production théorique
	 * @param f le type de fève
	 * @param etape l'étape considéré
	 * @return le stock prévisionnel de fève de type f sans prendre en compte les ventes à la bourse et les nouveaux contrats cadre
	 */
	protected HashMap<Feve,Double> getStockTheo(int etape) {
		if (etape < Filiere.LA_FILIERE.getEtape())
			return 0;
		HashMap<Feve, Lot> stocksTheo = this.createStocks();
		
		for (Feve f: this.stocks.keySet())
			stocksTheo.get(f).ajouter(this.stocks.get(f));
		
		HashMap<Feve, Double> varQuantite = createStock();
		
		//ajouter prod de ce tour si pas encore fait
		for (ExemplaireContratCadre exCC : this.contrats)
			varQuantite.put((Feve)exCC.getProduit(), varQuantite.get((Feve) exCC.getProduit()) - exCC.getQuantiteALivrerAuStep());
		
		HashMap<Feve, Double> quantiteRetard = createStock();
		
		
		
		
		for (int curEtape = Filiere.LA_FILIERE.getEtape() + 1; curEtape <= etape; curEtape ++) {
			this.majPerim(stocksTheo, curEtape);
			
			HashMap<Feve, Double> varQuantite2 = createStock();
			
			for (ExemplaireContratCadre exCC : this.contrats) {
				Feve f = (Feve) exCC.getProduit();
				varQuantite2.put(f, varQuantite2.get(f) - exCC.getEcheancier().getQuantite(curEtape));
			}
				
			
		}
	}
	
	
	private void variaQuant(HashMap<Feve, Double> varQuantite, HashMap<Feve, Lot> stocks, HashMap<Feve, Double> quantiteRetard) {
		for (Feve f: varQuantite.keySet()) {
			if (varQuantite.get(f) < 0) {
				if (-varQuantite.get(f) > stocks.get(f).getQuantiteTotale()) {
					quantiteRetard.put(f, -varQuantite.get(f) - stocks.get(f).getQuantiteTotale());
					stocks.get(f).retirer(stocks.get(f).getQuantiteTotale());
				}
				else {
					stocks.get(f).retirer(-varQuantite.get(f));
				}
			}
			else if(varQuantite.get(f) > 0)
			{
				stocks.get(f).ajouter(Filiere.LA_FILIERE.getEtape(), varQuantite.get(f));
			}
		}
	}
	
	/**
	 * Ajoute le lot au stock
	 * @param lot le lot à ajouter au stock
	 */
	protected void ajouterStock(Lot lot) {
		stocks.get((Feve)lot.getProduit()).ajouter(lot);
		this.majTot((Feve)lot.getProduit());
	}
	
	/**
	 * Ajoute la quantité quantite de fève de type f produites à l'étape etapeProd au stock
	 * @param f le type de fève
	 * @param etapeProd l'étape de production des fèves
	 * @param quantite la quantité de fève
	 */
	protected void ajouterStock(Feve f, int etapeProd, double quantite) {
		stocks.get(f).ajouter(etapeProd, quantite);
		this.majTot(f);
	}
	
	/**
	 * Retire une quantité quantite de fève de type f du stock
	 * @param f le type de fève
	 * @param quantite la quantité de fève
	 * @return un lot contenant la quantité voulu de fève de type f
	 */
	protected Lot retirerStock(Feve f, double quantite) {
		Lot res = this.stocks.get(f).retirer(quantite);
		this.majTot(f);
		return res;
	}
	
	/**
	 * Renvoie une chaîne de caractères décrivant le stock actuel en détails
	 * @return la description du stock actuel
	 */
	protected String stocksString() {
		return "Stock : \nBQ : " + this.stocks.get(Feve.F_BQ)
				+ "\nMQ : " + this.stocks.get(Feve.F_MQ)
				+ "\nMQ_BE : " + this.stocks.get(Feve.F_MQ_BE)
				+ "\nHQ_BE : " + this.stocks.get(Feve.F_HQ_BE);
	}
	
	/**
	 * Renvoie une chaîne de caractères décrivant le stock total de chaque type de fève
	 * @return la description des quantités stockées de chaque type de fève
	 */
	protected String stocksTotString() {
		return "Stock : \nBQ : " + this.stocksTot.get(Feve.F_BQ).getValeur()
		+ "\nMQ : " + this.stocksTot.get(Feve.F_MQ).getValeur()
		+ "\nMQ_BE : " + this.stocksTot.get(Feve.F_MQ_BE).getValeur()
		+ "\nHQ_BE : " + this.stocksTot.get(Feve.F_HQ_BE).getValeur();
	}
}
