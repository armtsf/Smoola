package ast.node.expression;

import ast.Visitor;

public class This extends Expression {
    @Override
    public String toString() {
        return "This";
    }
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getGeneratedCode() {
        return "aload_0\n";
    }
}
