package ast.node.expression.Value;

import ast.Type.Type;
import ast.Visitor;

public class BooleanValue extends Value {
    private boolean constant;

    public BooleanValue(boolean constant, Type type) {
        this.constant = constant;
        this.type = type;
    }

    public boolean isConstant() {
        return constant;
    }

    public void setConstant(boolean constant) {
        this.constant = constant;
    }

    @Override
    public String toString() {
        return "BooleanValue " + constant;
    }
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getGeneratedCode() {
        if (constant)
            return "iconst_1\n";
        else
            return "iconst_0\n";
    }
}
