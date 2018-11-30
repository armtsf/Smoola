package ast;

import ast.node.Program;
import ast.node.declaration.ClassDeclaration;
import ast.node.declaration.MethodDeclaration;
import ast.node.declaration.VarDeclaration;
import ast.node.expression.*;
import ast.node.expression.Value.BooleanValue;
import ast.node.expression.Value.IntValue;
import ast.node.expression.Value.StringValue;
import ast.node.statement.*;
import symbolTable.ItemAlreadyExistsException;
import symbolTable.SymbolTable;
import symbolTable.SymbolTableClassItem;

public class VisitorImpl implements Visitor {

    private Pass pass;
    boolean hasError;

    public VisitorImpl() {
        pass = Pass.First;
        hasError = false;
    }

    public void setPass(Pass newPass) {
        pass = newPass;
    }

    @Override
    public void visit(Program program) {
        if (pass == Pass.PrintOrder)
            System.out.println(program.toString());
        if (pass == Pass.First) {
            SymbolTable symbolTable = new SymbolTable();
            SymbolTable.push(symbolTable);
        }
        program.getMainClass().accept(this);
        for (ClassDeclaration classDec : program.getClasses()) {
            classDec.accept(this);
        }
    }

    @Override
    public void visit(ClassDeclaration classDeclaration) {
        if (pass == Pass.PrintOrder)
            System.out.println(classDeclaration.toString());

        if (pass == Pass.First) {
            SymbolTableClassItem symbolTableClassItem = new SymbolTableClassItem(classDeclaration.getName().getName());
            try {
                SymbolTable.top.put(symbolTableClassItem);
            } catch (ItemAlreadyExistsException e) {
                ErrorLogger.log("Redefinition of class "+classDeclaration.getName().getName(), classDeclaration);
                hasError = true;
            }
        }

        if (pass != Pass.First) {
            for (VarDeclaration varDec : classDeclaration.getVarDeclarations()) {
                varDec.accept(this);
            }
            for (MethodDeclaration methodDec : classDeclaration.getMethodDeclarations()) {
                methodDec.accept(this);
            }
        }
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration) {
        if (pass == Pass.PrintOrder)
            System.out.println(methodDeclaration.toString());
        for (VarDeclaration arg : methodDeclaration.getArgs()) {
            arg.accept(this);
        }
        for (VarDeclaration localVar : methodDeclaration.getLocalVars()) {
            localVar.accept(this);
        }
        for (Statement statement : methodDeclaration.getBody()) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(VarDeclaration varDeclaration) {
        if (pass == Pass.PrintOrder)
            System.out.println(varDeclaration.toString());
        varDeclaration.getIdentifier().accept(this);
    }

    @Override
    public void visit(ArrayCall arrayCall) {
        if (pass == Pass.PrintOrder)
            System.out.println(arrayCall.toString());
        arrayCall.getInstance().accept(this);
        arrayCall.getIndex().accept(this);
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        if (pass == Pass.PrintOrder)
            System.out.println(binaryExpression.toString());
        binaryExpression.getLeft().accept(this);
        binaryExpression.getRight().accept(this);
    }

    @Override
    public void visit(Identifier identifier) {
        if (pass == Pass.PrintOrder)
            System.out.println(identifier.toString());
    }

    @Override
    public void visit(Length length) {
        if (pass == Pass.PrintOrder)
            System.out.println(length.toString());
        length.getExpression().accept(this);
    }

    @Override
    public void visit(MethodCall methodCall) {
        if (pass == Pass.PrintOrder)
            System.out.println(methodCall.toString());
        methodCall.getInstance().accept(this);
        methodCall.getMethodName().accept(this);
        for (Expression arg : methodCall.getArgs()) {
            arg.accept(this);
        }
    }

    @Override
    public void visit(NewArray newArray) {
        if (pass == Pass.PrintOrder)
            System.out.println(newArray.toString());
        newArray.getExpression().accept(this);
    }

    @Override
    public void visit(NewClass newClass) {
        if (pass == Pass.PrintOrder)
            System.out.println(newClass.toString());
        newClass.getClassName().accept(this);
    }

    @Override
    public void visit(This instance) {
        if (pass == Pass.PrintOrder)
            System.out.println(instance.toString());
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        if (pass == Pass.PrintOrder)
            System.out.println(unaryExpression.toString());
        unaryExpression.getValue().accept(this);
    }

    @Override
    public void visit(BooleanValue value) {
        if (pass == Pass.PrintOrder)
            System.out.println(value.toString());
    }

    @Override
    public void visit(IntValue value) {
        if (pass == Pass.PrintOrder)
            System.out.println(value.toString());
    }

    @Override
    public void visit(StringValue value) {
        if (pass == Pass.PrintOrder)
            System.out.println(value.toString());
    }

    @Override
    public void visit(Assign assign) {
        if (pass == Pass.PrintOrder)
            System.out.println(assign.toString());
        assign.getlValue().accept(this);
        assign.getrValue().accept(this);
    }

    @Override
    public void visit(Block block) {
        if (pass == Pass.PrintOrder)
            System.out.println(block.toString());
        for (Statement statement : block.getBody()) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(Conditional conditional) {
        if (pass == Pass.PrintOrder)
            System.out.println(conditional.toString());
        conditional.getExpression().accept(this);
        conditional.getConsequenceBody().accept(this);
        if (conditional.getAlternativeBody() != null) {
            conditional.getAlternativeBody().accept(this);
        }
    }

    @Override
    public void visit(While loop) {
        if (pass == Pass.PrintOrder)
            System.out.println(loop.toString());
        loop.getCondition().accept(this);
        loop.getBody().accept(this);
    }

    @Override
    public void visit(Write write) {
        if (pass == Pass.PrintOrder)
            System.out.println(write.toString());
        write.getArg().accept(this);
    }
}
