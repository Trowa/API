package esialrobotik.ia.actions.a2018;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import esialrobotik.ia.actions.ActionExecutor;
import esialrobotik.ia.utils.communication.raspberry.Serial;

/**
 * 
 * @author gryttix
 *
 */
public abstract class ActionAX12Abstract implements ActionExecutor, AX12Link {

	// La liaison s�rie vers les AX12
    private Serial serialAX12;
    // Pour �viter un getOutputStream() � chaque fois qu'on a besoin d'�crire
    private OutputStream os;
    // Pour �viter un getInputStream() � chaque fois qu'on a besoin de lire
    private InputStream is;
    
    // Utilis� pour la lecture des r�ponses des ax12
    protected ArrayList<Byte> lecture;
    // Permet d'utiliser une liaison half duplex en reliant les pins rx et tx de l'uart entre elles
    protected boolean combinedRxTx = true;
    
    // Une seule instance de l'ax12 : on change son adresse pour chaque commande
    private AX12 ax12;
    
    protected boolean fini = false;
    
    protected enum AX12_NAME {
    	RAIL(1),
    	ORIENTATION(2),
    	PENTE(3),
    	BRAS_GAUCHE(4),
    	BRAS_DROIT(5);
    	
    	public final int adresse;
    	private AX12_NAME(int adresse) {
    		this.adresse = adresse;
    	}
    }
    
    // Les diverses actions possibles par AX12
	protected enum ACTION_AX12 {
		// G�re la cr�maill�re pour faire tranlster les tubes
		EAU_RAIL_GARAGE(AX12_NAME.RAIL, 267.7),
		EAU_RAIL_REMPLISSAGE_1(AX12_NAME.RAIL, 173.0),
		EAU_RAIL_REMPLISSAGE_2(AX12_NAME.RAIL, 66.4),
		EAU_RAIL_MILIEU_VIDANGE(AX12_NAME.RAIL, 131.4),
		EAU_RAIL_LANCEUR_GAUCHE(AX12_NAME.RAIL, 300.0),
		EAU_RAIL_LANCEUR_DROIT(AX12_NAME.RAIL, 183.9),
		EAU_RAIL_EXTREME_GAUCHE(AX12_NAME.RAIL, 0.0),
		
		// Permet de faire tourner les tubes comme des aiguilles sur un cadran d'horloge
		EAU_ORIENTATION_DROIT(AX12_NAME.ORIENTATION, 151.5),
		EAU_ORIENTATION_VIDANGE_GAUCHE(AX12_NAME.ORIENTATION, 248.1),
		EAU_ORIENTATION_VIDANGE_DROIT(AX12_NAME.ORIENTATION, 52.5),
		EAU_ORIENTATION_LANCEUR_GAUCHE(AX12_NAME.ORIENTATION, 146.8),
		EAU_ORIENTATION_LANCEUR_DROIT(AX12_NAME.ORIENTATION, 146.9),
		EAU_ORIENTATION_HORIZONTAL_GAUCHE(AX12_NAME.ORIENTATION, 240.2),
		EAU_ORIENTATION_HORIZONTAL_DROIT(AX12_NAME.ORIENTATION, 60.3),
		
		// G�re l'inclinaison des tubes � l'int�rieur du robot
		EAU_PENTE_HORIZONTALE(AX12_NAME.PENTE, 142.3),
		EAU_PENTE_QUASI_HORIZONTALE(AX12_NAME.PENTE, 150.0),
		EAU_PENTE_VERTICALE(AX12_NAME.PENTE, 231.7),
		EAU_PENTE_DOUCE(AX12_NAME.PENTE, 139.6),
		EAU_PENTE_FORTE(AX12_NAME.PENTE, 130.2),
		EAU_PENTE_REMPLISSAGE(AX12_NAME.PENTE, 242.3),
		// TODO EAU_PENTE_ALLUMAGE_PANNEAU_DOMOTIQUE(AX12_NAME.PENTE, ???);
		
		// G�re le bras gauche du robot (du point de vue du robot)
		BRAS_GAUCHE_SORTIR(AX12_NAME.BRAS_GAUCHE, 145.7),
		BRAS_GAUCHE_RENTRER(AX12_NAME.BRAS_GAUCHE, 242.0),
		
		// G�re le bras droit du robot (du point de vue du robot)
		BRAS_DROIT_SORTIR(AX12_NAME.BRAS_DROIT, 247.7),
		BRAS_DROIT_RENTRER(AX12_NAME.BRAS_DROIT, 153.5);
		
		public final AX12_NAME ax12;
		public final double angle;
		ACTION_AX12(AX12_NAME ax12, double angle) {
			this.ax12 = ax12;
			this.angle = angle;
		}
	}

    public ActionExecutor init(Serial serialAX12) {
        this.serialAX12 = serialAX12;
        this.is = serialAX12.getInputStream();
        this.os = serialAX12.getOutputStream();
        this.combinedRxTx = true;
        ax12 = new AX12(1, this);
        return this;
    }
    
    @Override
    public void execute() {
		fini = false;
    	this.childExecution();
    	fini = true;
    }
    
    @Override
    public boolean finished() {
        return fini;
    }

	@Override
	public byte[] sendCommand(byte[] cmd, int baudRate) throws AX12LinkException {
		int oldBr = -1;
		byte[] response = null;
		
		try {
			os.write(cmd);
			os.flush();
			
			// On retire du flux d'entr�e la commande qu'on vient juste d'envoyer si rx et tx sont combin�s
			if (this.combinedRxTx) {
				for (int i=0; i<cmd.length; i++) {
					if (is.read() == -1) {
						throw new AX12LinkException("Erreur de vidange du flux d'entr�e. Rx et Tx sont-ils vraiment reli�s entre eux ?");
					}
				}	
			}
			
			// On lit la r�ponse de l'AX12
			this.lecture.clear();
			int r;
			while ((r = is.read()) != -1) {
				lecture.add(AX12.intToUnsignedByte(r));
			}
			
			response = new byte[lecture.size()];
			for (int i=lecture.size()-1; i>=0; i--) {
				response[i] = lecture.get(i);
			}
			
		} catch (IOException e1) {
			throw new AX12LinkException("Erreur de transmission de la commande", e1);
		} finally {
			if (oldBr != -1) {
				this.setBaudRate(oldBr);
			}
		}
		
		return response;
	}
	
	@Override
	public void enableDTR(boolean acitvate) {
		serialAX12.setDTR(acitvate);
	}

	@Override
	public int getBaudRate() {
		return 115200;
	}

	@Override
	public void setBaudRate(int baudRate) throws AX12LinkException {
		// Nothing
	}
	
	/**
	 * Applique l'�tat demand�
	 * Cette fonction s'appelle go parce que do est d�j� pris :'(
	 * @param et
	 */
	protected void go(ACTION_AX12 et) {
		if (ax12 == null) {
			return;
		}
		
		ax12.setAddress(et.ax12.adresse);
		try {
			// Ptit hack d�gueu : on ajoute de l'�lasticit� � l'ax12 qui l�ve les tubes
			// �a �vite de perdre les balles du dessus � cause des secousses
			if (et.ax12 == AX12_NAME.PENTE) {
				ax12.setCwComplianceSlope(99);
				ax12.setCcwComplianceSlope(99);
			}
			ax12.setServoPositionInDegrees(et.angle);
		} catch (AX12LinkException e) {
			e.printStackTrace();
		} catch (AX12Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Attend une certaine dur�e en ms
	 * @param duree tps � attendre en ms
	 */
	protected void attend(long duree) {
		try {
			Thread.sleep(duree);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Attend que tous les ax12 de la liste aient fini de bouger
	 * Attention aux blagues avec le mode rotation continue ;)
	 * @param ax12
	 */
	protected void attendreImmobilisation(AX12_NAME... liste) {
		boolean bouge = false;
		
		do {
			if (bouge) {
				// Pour �viter de spammer la liaison s�rie, on est pas � 50ms pr�s
				attend(50);
			}
			bouge = false;
			for (AX12_NAME ax : liste) {
				ax12.setAddress(ax.adresse);
				try {
					if (ax12.isMoving()) {
						bouge = true;
						break;
					}
				} catch (AX12LinkException e) {
					e.printStackTrace();
				} catch (AX12Exception e) {
					e.printStackTrace();
				}
			}
		} while (bouge);
	}
    
	/**
	 * Les commandes utiles des classes enfant
	 */
    protected abstract void childExecution();
    
    /**
     * Allume ou �teint le lanceur
     * Oui �a n'a normalement rien � voir avec un AX12 mais c'est contr�l� par le pin DTR de l'UART :p
     * @param allumer
     */
    protected void allumerLanceur(boolean allumer) {
    	this.serialAX12.setDTR(allumer);
    }

	@Override
	public void resetActionState() {
		this.fini = false;
	}

}