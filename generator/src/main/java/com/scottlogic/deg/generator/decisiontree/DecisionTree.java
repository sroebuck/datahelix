package com.scottlogic.deg.generator.decisiontree;

import com.scottlogic.deg.common.profile.ProfileFields;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSConstraintNode;

public class DecisionTree {
    public final FSConstraintNode rootNode;
    public final ProfileFields fields;
    public final String description;

    public DecisionTree(FSConstraintNode rootNode, ProfileFields fields, String description) {
        this.rootNode = rootNode;
        this.fields = fields;
        this.description = description;
    }

    public FSConstraintNode getRootNode() {
        return rootNode;
    }

    public String getDescription(){
        return description;
    }

    public ProfileFields getFields() {
        return fields;
    }

    public String toString(){
        return description;
    }
}

