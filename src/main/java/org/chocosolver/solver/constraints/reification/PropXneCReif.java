/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.util.ESat;

/**
 * A propagator dedicated to express in a compact way: (x &ne; c) &hArr; b
 *
 * @author Charles Prud'homme
 * @since 03/05/2016.
 */
public class PropXneCReif extends Propagator<IntVar> {

    IntVar var;
    int cste;
    BoolVar r;

    public PropXneCReif(IntVar x, int c, BoolVar r) {
        super(new IntVar[]{x, r}, PropagatorPriority.BINARY, false);
        this.cste = c;
        this.var = x;
        this.r = r;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (r.getLB() == 1) {
            if (var.removeValue(cste, this)) {
                setPassive();
            }
        } else if (r.getUB() == 0) {
            setPassive();
            var.instantiateTo(cste, this);
        } else {
            if (!var.contains(cste)) {
                setPassive();
                r.setToTrue(this);
            } else if (var.isInstantiatedTo(cste)) {
                setPassive();
                r.setToFalse(this);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if(isCompletelyInstantiated()){
            if(r.isInstantiatedTo(1)){
                return ESat.eval(!var.contains(cste));
            }else{
                return ESat.eval(var.contains(cste));
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean nrules = ruleStore.addPropagatorActivationRule(this);
        if (var == vars[1]) {
            if (vars[1].isInstantiatedTo(1)) {
                nrules |= ruleStore.addRemovalRule(vars[0], cste);
            } else {
                nrules |= ruleStore.addFullDomainRule(vars[0]);
            }
        } else {
            nrules |= ruleStore.addFullDomainRule(vars[1]);
        }
        return nrules;
    }

    @Override
    public String toString() {
        return "(" + var.getName() +" != " + cste + ") <=> "+r.getName();
    }
}
