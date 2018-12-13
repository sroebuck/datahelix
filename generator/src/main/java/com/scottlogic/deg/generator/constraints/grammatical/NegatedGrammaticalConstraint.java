package com.scottlogic.deg.generator.constraints.grammatical;

import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.inputs.RuleInformation;

import java.util.Collection;
import java.util.Objects;

public class NegatedGrammaticalConstraint implements GrammaticalConstraint {
    public final GrammaticalConstraint negatedConstraint;

    NegatedGrammaticalConstraint(GrammaticalConstraint negatedConstraint) {
        if (negatedConstraint instanceof NegatedGrammaticalConstraint)
            throw new IllegalArgumentException("nested NegatedGrammatical constraint not allowed");
        this.negatedConstraint = negatedConstraint;
    }

    @Override
    public GrammaticalConstraint negate() {
        return this.negatedConstraint;
    }

    private GrammaticalConstraint getBaseConstraint(){
        return negatedConstraint;
    }

    public String toString(){
        return String.format(
                "NOT(%s)",
                negatedConstraint);
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NegatedGrammaticalConstraint otherConstraint = (NegatedGrammaticalConstraint) o;
        return Objects.equals(getBaseConstraint(), otherConstraint.getBaseConstraint());
    }

    @Override
    public int hashCode(){
        return Objects.hash("NOT", negatedConstraint.hashCode());
    }

    @Override
    public Collection<Field> getFields() {
        return negatedConstraint.getFields();
    }

    @Override
    public RuleInformation getRule() {
        return negatedConstraint.getRule();
    }
}
