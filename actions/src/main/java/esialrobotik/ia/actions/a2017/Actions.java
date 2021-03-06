package esialrobotik.ia.actions.a2017;

import com.pi4j.io.serial.Baud;
import esialrobotik.ia.actions.ActionExecutor;
import esialrobotik.ia.actions.ActionInterface;
import esialrobotik.ia.actions.ActionModuleConfiguration;
import esialrobotik.ia.actions.a2017.bras.*;
import esialrobotik.ia.actions.a2017.minerai.MineraiLarguer;
import esialrobotik.ia.actions.a2017.minerai.MineraiRamasser;
import esialrobotik.ia.actions.a2017.minerai.MineraiRentrer;
import esialrobotik.ia.utils.communication.raspberry.Serial;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by franc on 19/05/2017.
 */
public class Actions implements ActionInterface {

    private Serial serialAX12;

    private List<ActionExecutor> actionExecutors;

    @Inject
    public Actions(ActionModuleConfiguration actionModuleConfiguration) {
        this.serialAX12 = new Serial(actionModuleConfiguration.getSerialPort(), Baud.getInstance(actionModuleConfiguration.getBaud()));
        // On instancie les différents types d'actions, on fait les init et on stocke tout ça dans la liste
        actionExecutors = new ArrayList<>();

        /*
         * 0 - Ramasser du minerai
         * 1 - Largueur du minerai dans la zone de départ
         * 2 - Rentrer le ramasse minerai
         */
        actionExecutors.add(new MineraiRamasser().init(this.serialAX12));
        actionExecutors.add(new MineraiLarguer().init(this.serialAX12));
        actionExecutors.add(new MineraiRentrer().init(this.serialAX12));

        /*
         * 3 - Sortir le bras
         * 4 - Saisir un module
         * 5 - Rentrer le bras
         * 6 - Lacher un module à l'horizontale
         * 7 - Lacher un module à la verticale
         */
        actionExecutors.add(new BrasSortir().init(this.serialAX12));
        actionExecutors.add(new BrasSaisirModule().init(this.serialAX12));
        actionExecutors.add(new BrasRentrer().init(this.serialAX12));
        actionExecutors.add(new BrasLacherModuleHorizontal().init(this.serialAX12));
        actionExecutors.add(new BrasLacherModuleVertical().init(this.serialAX12));
    }

    @Override
    public ActionExecutor getActionExecutor(int id) {
        return this.actionExecutors.get(id);
    }

}
