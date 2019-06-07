package com.scottlogic.deg.generator.walker.reductive.fieldselectionstrategy;


import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSConstraintNode;

public class FixFieldStrategyFactory {
    public FixFieldStrategy create(FSConstraintNode rootNode){
        return new FieldAppearanceFixingStrategy(rootNode);
    }
}