package ast;

import ast.Type.ArrayType.ArrayType;
import ast.Type.PrimitiveType.BooleanType;
import ast.Type.PrimitiveType.IntType;
import ast.node.Program;
import ast.node.declaration.ClassDeclaration;
import ast.node.declaration.MethodDeclaration;
import ast.node.declaration.VarDeclaration;
import ast.node.expression.*;
import ast.node.expression.Value.BooleanValue;
import ast.node.expression.Value.IntValue;
import ast.node.expression.Value.ObjectValue;
import ast.node.expression.Value.StringValue;
import ast.node.statement.*;
import symbolTable.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class GeneratorVisitorImpl implements Visitor {

    private HashMap<String, SymbolTable> classSymbolTable;
    private HashMap<String, ClassDeclaration> classDecMap;
    private ArrayList<String> generatedCode = new ArrayList<>();
    private boolean classVar = false;
    private int variableIndex = 0;
    private String curClassName;

    public void setClassSymbolTable(HashMap<String, SymbolTable> classSymbolTable) { this.classSymbolTable = classSymbolTable; }

    public void writeToFile(String name) {
        try {
            Path file = Paths.get("./generated/" + name + ".j");
            Files.write(file, generatedCode);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(Program program) {
        String mainClass = program.getMainClass().getName().getName();
        starterClassCodeGenerator(mainClass);
        writeToFile("GeneratedMainClass");

        generatedCode = new ArrayList<>();
        program.getMainClass().accept(this);
        writeToFile(mainClass);

        for (ClassDeclaration classDec : program.getClasses()) {
            generatedCode = new ArrayList<>();
            classDec.accept(this);
            writeToFile(classDec.getName().getName());
        }
    }

    @Override
    public void visit(ClassDeclaration classDeclaration) {
        SymbolTable symbolTable = new SymbolTable(SymbolTable.top);
        SymbolTable.push(symbolTable);

        generatedCode.addAll(classDeclaration.getGeneratedCode());

        String initCode = "";
        classVar = true;
        curClassName = classDeclaration.getName().getName();
        for (VarDeclaration varDec : classDeclaration.getVarDeclarations()) {
            varDec.accept(this);
//            if (varDec.getType().subtype(new StringType()))
//                initCode += "aload_0\n" + "ldc \n" + "putfield " + classDeclaration.getName().getName() + "/" +
//                        varDec.getType().getTypeCode() + "\n";
//            else if (varDec.getType().subtype(new IntType()) || varDec.getType().subtype(new BooleanType()))
//                initCode += "aload_0\n" + "iconst_0\n" + "putfield " + classDeclaration.getName().getName() + "/" +
//                        varDec.getType().getTypeCode() + "\n";
        }
        classVar = false;
       // generatedCode.add(initCode);

        generatedCode.addAll(classDeclaration.getInitMethodDecCode());

        for (MethodDeclaration methodDec : classDeclaration.getMethodDeclarations()) {
            generatedCode.addAll(methodDec.getGeneratedCode());
            methodDec.accept(this);
        }

        SymbolTable.pop();
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration) {
        SymbolTable symbolTable = new SymbolTable(SymbolTable.top);
        SymbolTable.push(symbolTable);

        variableIndex = 1;

        for (VarDeclaration arg : methodDeclaration.getArgs()) {
            arg.accept(this);
            variableIndex++;
        }
        for (VarDeclaration localVar : methodDeclaration.getLocalVars()) {
            localVar.accept(this);
            variableIndex++;
        }
        for (Statement statement : methodDeclaration.getBody()) {
            statement.accept(this);
        }

        methodDeclaration.getReturnValue().accept(this);
        generatedCode.add(methodDeclaration.getReturnCode());
        generatedCode.add(".end method");

        SymbolTable.pop();
    }

    @Override
    public void visit(VarDeclaration varDeclaration) {
        int index = -1;
        if (!classVar) {
//            if (varDeclaration.getType().subtype(new BooleanType())) {
//                generatedCode.add("iconst_0\n" +
//                        "istore " + String.valueOf(symbolTableVariableItem.getIndex()) + "\n");
//            }
//            else if (varDeclaration.getType().subtype(new IntType())) {
//                generatedCode.add("iconst_0\n" +
//                        "istore " + String.valueOf(symbolTableVariableItem.getIndex()) + "\n");
//            }
//            else if (varDeclaration.getType().subtype(new StringType())) {
//                generatedCode.add("ldc \"\"\n" +
//                        "astore " + String.valueOf(symbolTableVariableItem.getIndex()) + "\n");
//            }
        }
        else {
            index = variableIndex;
            generatedCode.addAll(varDeclaration.getGeneratedCode());
        }
        String varName = varDeclaration.getIdentifier().getName();
        SymbolTableVariableItem symbolTableVariableItem = new SymbolTableVariableItem(varName, varDeclaration.getType(), index);
        try {
            SymbolTable.top.put(symbolTableVariableItem);
        } catch (ItemAlreadyExistsException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void visit(ArrayCall arrayCall) {
        arrayCall.getInstance().accept(this);
        arrayCall.getIndex().accept(this);
        generatedCode.addAll(arrayCall.getGeneratedCode());
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        binaryExpression.getLeft().accept(this);
        binaryExpression.getRight().accept(this);
        generatedCode.addAll(binaryExpression.getGeneratedCode());
    }

    @Override
    public void visit(Identifier identifier) {
        try {
            SymbolTableVariableItem item = (SymbolTableVariableItem) SymbolTable.top.get(identifier.getName());
            if (item.getIndex() == -1) {
                generatedCode.add("aload_0");
                generatedCode.add("getfield " + curClassName + "/" + identifier.getName() + " " + identifier.getType().getTypeCode());
            } else {
                if (item.getType().subtype(new IntType()) || item.getType().subtype(new BooleanType())) {
                    generatedCode.add("iload " + item.getIndex());
                } else {
                    generatedCode.add("aload " + item.getIndex());
                }
            }
        }
        catch (ItemNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(Length length) {
        generatedCode.addAll(length.getGeneratedCode());
    }

    @Override
    public void visit(MethodCall methodCall) {
        methodCall.getInstance().accept(this);

        MethodDeclaration methodDec = null;
        ClassDeclaration classDec = classDecMap.get(methodCall.getInstance().getType().toString());
        while (classDec != null) {
            if (classDec.containsMethod(methodCall.getMethodName()))
                methodDec = classDec.getMethodDeclaration(methodCall.getMethodName());
            else
                classDec = classDecMap.get(classDec.getParentName().getName());
        }

        for (Expression arg : methodCall.getArgs()) {
            arg.accept(this);
        }

        generatedCode.add("invokevirtual " + methodCall.getInstance().getType().toString() + "/"
                + methodDec.getInvokationCode());

    }

    @Override
    public void visit(NewArray newArray) {
        newArray.getExpression().accept(this);
        generatedCode.addAll(newArray.getGeneratedCode());
    }

    @Override
    public void visit(NewClass newClass) {
        generatedCode.addAll(newClass.getGeneratedCode());
        ClassDeclaration classDec = classDecMap.get(newClass.getClassName().getName());
        generatedCode.add(classDec.getInitMethod());
    }

    @Override
    public void visit(This instance) {
        generatedCode.addAll(instance.getGeneratedCode());
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        unaryExpression.getValue().accept(this);
    }

    @Override
    public void visit(BooleanValue value) {
        generatedCode.addAll(value.getGeneratedCode());
    }

    @Override
    public void visit(IntValue value) {
        generatedCode.addAll(value.getGeneratedCode());
    }

    @Override
    public void visit(StringValue value) {
        generatedCode.addAll(value.getGeneratedCode());
    }

    @Override
    public void visit(ObjectValue value) {
        //TODO
    }

    @Override
    public void visit(Assign assign) {
        Expression lvalue = assign.getlValue();
        if (lvalue instanceof Identifier) {
            Identifier identifier = (Identifier)lvalue;
            SymbolTableVariableItem item;
            try {
                item = (SymbolTableVariableItem)SymbolTable.top.get(identifier.getName());
            } catch (ItemNotFoundException e) {
                e.printStackTrace();
                return;
            }
            if (item.getIndex() == -1) {
                generatedCode.add("aload_0");
                assign.getrValue().accept(this);
                generatedCode.add("putfield " + curClassName + "/" + identifier.getName() + " " + identifier.getType().getTypeCode());
            } else {
                assign.getrValue().accept(this);
                if (lvalue.getType().subtype(new IntType()) || lvalue.getType().subtype(new BooleanType())) {
                    generatedCode.add("istore " + String.valueOf(item.getIndex()));
                } else {
                    generatedCode.add("astore " + String.valueOf(item.getIndex()));
                }
            }
        } else if (lvalue instanceof ArrayCall) {
            lvalue.accept(this);
            assign.getrValue().accept(this);
            generatedCode.add("iastore");
        }
    }

    @Override
    public void visit(Block block) {
        for (Statement statement : block.getBody()) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(Conditional conditional) {
        conditional.getExpression().accept(this);
        conditional.getConsequenceBody().accept(this);
        if (conditional.getAlternativeBody() != null) {
            conditional.getAlternativeBody().accept(this);
        }
    }

    @Override
    public void visit(MethodCallInMain methodCallInMain) {
        methodCallInMain.getInstance().accept(this);
    }

    @Override
    public void visit(While loop) {
        loop.getCondition().accept(this);
        loop.getBody().accept(this);
    }

    @Override
    public void visit(Write write) {
        generatedCode.add(write.getPrintStream());
        write.getArg().accept(this);
        generatedCode.add(write.getInvokeCode());
    }

    public void starterClassCodeGenerator(String mainClass) {
        String code = ".class public JavaMain\n" +
                ".super java/lang/Object\n" +
                ".method public <init>()V\n" +
                "aload_0 ; push this\n" +
                "invokespecial java/lang/Object/<init>()V ; call super\n" +
                "return\n" +
                ".end method\n" +
                ".method public static main([Ljava/lang/String;)V\n" +
                ".limit stack 2\n" +
                "new " + mainClass + " \n" +
                "dup\n" +
                "invokespecial " + mainClass + "/<init>()V\n" +
                "invokevirtual " + mainClass + "/main()I\n" +
                "return\n" +
                ".end method";
        generatedCode.add(code);
    }

    public void setClassDecMap(HashMap<String, ClassDeclaration> classDecMap) {
        this.classDecMap = classDecMap;
    }
}
