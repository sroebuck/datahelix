package com.scottlogic.deg.generator.decisiontree;

import com.scottlogic.deg.common.util.FlatMappingSpliterator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DecisionNode {
    private final Collection<ConstraintNode> options;

    public DecisionNode(ConstraintNode... options) {
        this(Collections.unmodifiableCollection(Arrays.asList(options)));
    }

    public DecisionNode(Collection<ConstraintNode> options) {
        this.options = Collections.unmodifiableCollection(options);
    }

    public Collection<ConstraintNode> getOptions() {
        return options;
    }

    public String toString(){
        return this.options.size() >= 5
            ? String.format("Options: %d", this.options.size())
            : String.format("Options [%d]: %s",
            this.options.size(),
            String.join(
                " OR ",
                this.options.stream().map(o -> o.toString()).collect(Collectors.toList())));
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionNode that = (DecisionNode) o;
        return Objects.equals(options, that.options);
    }

    public int hashCode() {
        return Objects.hash(options);
    }

    public DecisionNode accept(NodeVisitor visitor){
        Stream<ConstraintNode> options = getOptions().stream().map(c->c.accept(visitor));
        return visitor.visit(
            new DecisionNode(
                options.collect(Collectors.toSet())
            )
        );
    }
}

