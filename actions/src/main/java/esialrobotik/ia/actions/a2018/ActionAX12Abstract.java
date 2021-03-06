package esialrobotik.ia.actions.a2018;

import esialrobotik.ia.actions.ActionExecutor;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 * @author gryttix
 *
 */
public abstract class ActionAX12Abstract implements ActionExecutor, AX12Link {

	// La liaison serie vers les AX12
    private AX12Serial serialAX12;
    
    // Utilise pour la lecture des reponses des ax12
    protected ArrayList<Byte> lecture;
    
    // Une seule instance de l'ax12 : on change son adresse pour chaque commande
    private AX12 ax12;
    
    protected boolean fini = false;
    
    protected enum AX12_NAME {
    	RAIL(1),
    	PENTE(2),
    	ORIENTATION(3),
    	BRAS_GAUCHE(4),
    	BRAS_DROIT(5);
    	
    	public final int adresse;
    	private AX12_NAME(int adresse) {
    		this.adresse = adresse;
    	}
    }
    
    // Les diverses actions possibles par AX12
	protected enum ACTION_AX12 {
		// Gere la cremaillere pour faire translater les tubes
		EAU_RAIL_GARAGE(AX12_NAME.RAIL, 267.7),
		EAU_RAIL_REMPLISSAGE_1_A(AX12_NAME.RAIL, 173.0),
		EAU_RAIL_REMPLISSAGE_1_B(AX12_NAME.RAIL, 155.5),
		EAU_RAIL_REMPLISSAGE_2_A(AX12_NAME.RAIL, 57.5),
		EAU_RAIL_REMPLISSAGE_2_B(AX12_NAME.RAIL, 39.9),
		EAU_RAIL_MILIEU_VIDANGE(AX12_NAME.RAIL, 131.4),
		EAU_RAIL_LANCEUR_GAUCHE(AX12_NAME.RAIL, 300.0),
		EAU_RAIL_LANCEUR_DROIT(AX12_NAME.RAIL, 183.9),
		EAU_RAIL_EXTREME_GAUCHE(AX12_NAME.RAIL, 0.0),
		
		// Permet de faire tourner les tubes comme des aiguilles sur un cadran d'horloge
		EAU_ORIENTATION_DROIT(AX12_NAME.ORIENTATION, 151.5),
		EAU_ORIENTATION_VIDANGE_GAUCHE(AX12_NAME.ORIENTATION, 52.5),
		EAU_ORIENTATION_VIDANGE_DROIT(AX12_NAME.ORIENTATION, 248.1),
		EAU_ORIENTATION_LANCEUR_GAUCHE(AX12_NAME.ORIENTATION, 146.8),
		EAU_ORIENTATION_LANCEUR_DROIT(AX12_NAME.ORIENTATION, 146.9),
		EAU_ORIENTATION_HORIZONTAL_GAUCHE(AX12_NAME.ORIENTATION, 240.2),
		EAU_ORIENTATION_HORIZONTAL_DROIT(AX12_NAME.ORIENTATION, 60.3),
		EAU_ORIENTATION_REMPLISSAGE_INCLINAISON_GAUCHE(AX12_NAME.ORIENTATION, 143.5),
		EAU_ORIENTATION_REMPLISSAGE_INCLINAISON_DROITE(AX12_NAME.ORIENTATION, 159.5),
		
		// Gère l'inclinaison des tubes à l'interieur du robot
		EAU_PENTE_HORIZONTALE(AX12_NAME.PENTE, 142.3),
		EAU_PENTE_QUASI_HORIZONTALE(AX12_NAME.PENTE, 150.0),
		EAU_PENTE_VERTICALE(AX12_NAME.PENTE, 231.7),
		EAU_PENTE_DOUCE(AX12_NAME.PENTE, 139.6),
		EAU_PENTE_FORTE_GAUCHE(AX12_NAME.PENTE, 127.2),
		EAU_PENTE_FORTE_DROIT(AX12_NAME.PENTE, 124.5),
		EAU_PENTE_REMPLISSAGE(AX12_NAME.PENTE, 242.3),
		EAU_PENTE_INTERRUPTEUR(AX12_NAME.PENTE, 190.0),
		
		// Gere le bras gauche du robot (du point de vue du robot)
		BRAS_GAUCHE_SORTIR(AX12_NAME.BRAS_GAUCHE, 145.7),
		BRAS_GAUCHE_RENTRER(AX12_NAME.BRAS_GAUCHE, 242.0),
		
		// Gere le bras droit du robot (du point de vue du robot)
		BRAS_DROIT_SORTIR(AX12_NAME.BRAS_DROIT, 247.7),
		BRAS_DROIT_RENTRER(AX12_NAME.BRAS_DROIT, 153.5);
		
		public final AX12_NAME ax12;
		public final double angle;
		ACTION_AX12(AX12_NAME ax12, double angle) {
			this.ax12 = ax12;
			this.angle = angle;
		}
	}

    public ActionExecutor init(AX12Serial serialAX12) {
        this.serialAX12 = serialAX12;
        this.lecture = new ArrayList<Byte>();
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
	
	/**
	 * Applique l'etat demande
	 * Cette fonction s'appelle go parce que do est deja pris :'(
	 * @param et
	 */
	protected void go(ACTION_AX12 et) {
		if (ax12 == null) {
			return;
		}
		
		int essaisRestants = 5;
		
		ax12.setAddress(et.ax12.adresse);
		
		while(essaisRestants > 0) {
			try {
				// Ptit hack degueu : on ajoute de l'elasticite a l'ax12 qui lève les tubes
				// Ça evite de perdre les balles du dessus a cause des secousses
				if (et.ax12 == AX12_NAME.PENTE) {
					ax12.setCwComplianceSlope(99);
					ax12.setCcwComplianceSlope(99);
				}
				if (et.ax12 == AX12_NAME.ORIENTATION) {
					ax12.setCwComplianceSlope(100);
					ax12.setCcwComplianceSlope(100);
				}
				ax12.setServoPositionInDegrees(et.angle);
				essaisRestants = 0;
			} catch (AX12LinkException e) {
				e.printStackTrace();
				essaisRestants--;
				System.out.println("essais restant : "+essaisRestants);
			} catch (AX12Exception e) {
				e.printStackTrace();
				essaisRestants--;
				System.out.println("essais restant : "+essaisRestants);
			}	
		}
	}
	

    @Override
    public byte[] sendCommand(byte[] cmd, int baudRate) throws AX12LinkException {
        int oldBr = -1;
        byte[] response = null;

        try {
            serialAX12.write(cmd);
            serialAX12.flush();
            
            // On lit la reponse de l'AX12
            this.lecture.clear();
            int r;
            while ((r = serialAX12.read()) != -1) {
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
    public int getBaudRate() {
        return 115200;
    }

    @Override
    public void setBaudRate(int baudRate) throws AX12LinkException {
        // Nothing
    }

	
	/**
	 * Attend une certaine duree en ms
	 * @param duree tps a attendre en ms
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
		int maxExceptionTolerance = 10;
		
		do {
			if (bouge) {
				// Pour eviter de spammer la liaison serie, on est pas a 50ms près
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
					if (maxExceptionTolerance-- < 0) {
						bouge = true;
					}
				} catch (AX12Exception e) {
					e.printStackTrace();
					if (maxExceptionTolerance-- < 0) {
						bouge = true;
					}
				}
			}
		} while (bouge);
	}
    
	/**
	 * Les commandes utiles des classes enfant
	 */
    protected abstract void childExecution();
    
   /**
    * Allume ou eteint le lanceur
    * @param allumer
    * @throws AX12LinkException
    */
   @Override
   public void enableLanceur(boolean allumer) throws AX12LinkException {
       try {
           this.serialAX12.enableLanceur(allumer);
       } catch (AX12LinkException e) {
           e.printStackTrace();
       }
   }
   
    /**
     * Allume ou eteint le lanceur
     * @param allumer
     */
    protected void allumerLanceur(boolean allumer) {
    	try {
			this.serialAX12.enableLanceur(allumer);
		} catch (AX12LinkException e) {
			e.printStackTrace();
		}
    }

	@Override
	public void resetActionState() {
		this.fini = false;
	}

}
