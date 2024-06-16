package Languages.Java;

import Languages.C.C;
import Languages.Code;
import Languages.Rust.Rust;
import Transpiler.AbstractSyntaxTree;
import Transpiler.NodeType;
import Transpiler.RuleType;

import java.io.StringReader;

public class Java extends Code {

    public Java(String code) {
        super(code);
    }

    public Java(AbstractSyntaxTree ast) {
        super(ast);
    }

    @Override
    public AbstractSyntaxTree parseToAST() {
        /**
         * This function parses the given program code with Java Parser.
         * @return  Parsed AST (Abstract Syntax Tree) of the given Program.
         */
        JavaLexer lexer = new JavaLexer(new StringReader(this.code));
        JavaParser parser = new JavaParser(lexer);
        try {
            Object result = parser.parse().value;
            this.ast = (AbstractSyntaxTree) result;
            return this.ast;
        } catch (Exception e) {
            System.err.println("Parser error: " + e.getMessage());
            this.ast = null;
            return null;
        }
    }

    @Override
    public String generateCode() {
        /**
         * This function reverses the parsing process to generate a string code for the given AST.
         * @param tree: The AST (Abstract Syntax Tree) of the program we want to generate the code for.
         * @return  The generated C program code for the given AST.
         */
        StringBuilder code = new StringBuilder();
        switch (ast.getType()) {
            case PROGRAM ->{
                code.append("public static void ");
                code.append(new Java(ast.getChildren().getFirst()).generateCode());
                code.append("() {\n");
                code.append(new Java(ast.getChildren().get(1)).generateCode());
                code.append("}");
            }
            case STATEMENTS -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new Java(ast.getChildren().getFirst()).generateCode());
                        code.append(new Java(ast.getChildren().get(1)).generateCode());
                        code.append("\n");
                    }
                    case SINGLE -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                        code.append("\n");
                    }
                }
            }
            case FOLLOW_STATEMENTS -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append("{\n");
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                        code.append("}");
                        code.append("\n");
                    }
                    case SINGLE -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                    }
                    case EMPTY -> {
                        code.append("{}\n");
                    }
                }
            }
            case STATEMENT -> {
                switch (ast.getSubType()) {
                    case DECLARE, ASSIGNMENTS -> {
                        code.append(new Java(ast.getChildren().getFirst()).generateCode());
                        code.append(";");
                    }
                    case BREAK -> code.append("break;");
                    case CONTINUE -> code.append("continue;");
                    case PRINT -> {
                        code.append("System.out.println(");
                        code.append(new Java(ast.getChildren().getFirst()).generateCode());
                        code.append(");");
                    }
                    case IF, SWITCH, WHILE -> {
                        code.append(new Java(ast.getChildren().getFirst()).generateCode());
                    }
                }
            }
            case PRINT -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                        code.append(";\nSystem.out.println(");
                        code.append(new Rust(ast.getChildren().get(1)).generateCode());
                        code.append(")");
                    }
                    case SINGLE -> {
                        code.append(new Rust(ast.getChildren().getFirst()).generateCode());
                    }
                }
            }
            case DECLARATION -> {
                code.append("int ");
                code.append(new Java(ast.getChildren().get(0)).generateCode());
            }
            case ASSIGNMENTS -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                        code.append(" , ");
                        code.append(new Java(ast.getChildren().get(1)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case ASSIGNMENT -> {
                code.append(new Java(ast.getChildren().get(0)).generateCode());
                code.append(" = ");
                code.append(new Java(ast.getChildren().get(1)).generateCode());
            }
            case IF -> {
                code.append("if (");
                code.append(new Java(ast.getChildren().get(0)).generateCode());
                code.append(" ) ");
                code.append(new Java(ast.getChildren().get(1)).generateCode());
                code.append("else ");
                code.append(new Java(ast.getChildren().get(2)).generateCode());
            }
            case SWITCH -> {
                code.append("switch (");
                code.append(new Java(ast.getChildren().get(0)).generateCode());
                code.append(") {\n");
                code.append(new Java(ast.getChildren().get(1)).generateCode());
                code.append("}\n");
            }
            case CASES -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        if(ast.getChildren().get(0).getType().equals(NodeType.OPTIONS)) {
                            handleOptions(ast.getChildren().get(1), ast.getChildren().get(0), code);
                        }
                        else {
                            code.append("case ");
                            code.append(new Java(ast.getChildren().get(0)).generateCode());
                            code.append(" : ");
                            code.append(new Java(ast.getChildren().get(1)).generateCode());
                        }
                        code.append(new Java(ast.getChildren().get(2)).generateCode());
                    }
                    case DEFAULT -> {
                        code.append("default : ");
                        code.append(new Java(ast.getChildren().get(0)).generateCode());

                    }
                }
            }
            case WHILE -> {
                code.append("while (");
                code.append(new Java(ast.getChildren().get(0)).generateCode());
                code.append(") ");
                code.append(new Java(ast.getChildren().get(1)).generateCode());
            }
            case DISJUNCTION -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                        code.append(" || ");
                        code.append(new Java(ast.getChildren().get(1)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case CONJUNCTION -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                        code.append(" && ");
                        code.append(new Java(ast.getChildren().get(1)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case INVERSION -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append("! ");
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case COMPARISON, PRIMARY -> {
                code.append(new Java(ast.getChildren().get(0)).generateCode());
            }
            case EQ -> {
                code.append(new Java(ast.getChildren().get(0)).generateCode());
                code.append(" == ");
                code.append(new Java(ast.getChildren().get(1)).generateCode());
            }
            case LT -> {
                code.append(new Java(ast.getChildren().get(0)).generateCode());
                code.append("<");
                code.append(new Java(ast.getChildren().get(1)).generateCode());
            }
            case GT -> {
                code.append(new Java(ast.getChildren().get(0)).generateCode());
                code.append(">");
                code.append(new Java(ast.getChildren().get(1)).generateCode());
            }
            case SUM -> {
                switch (ast.getSubType()) {
                    case ADD -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                        code.append(" + ");
                        code.append(new Java(ast.getChildren().get(1)).generateCode());
                    }
                    case SUB -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                        code.append(" - ");
                        code.append(new Java(ast.getChildren().get(1)).generateCode());
                    }
                    case DEFAULT -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case TERM -> {
                switch (ast.getSubType()) {
                    case TIMES -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                        code.append(" * ");
                        code.append(new Java(ast.getChildren().get(1)).generateCode());
                    }
                    case DIVIDES -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                        code.append(" / ");
                        code.append(new Java(ast.getChildren().get(1)).generateCode());
                    }
                    case DEFAULT -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case MODULO -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                        code.append(" % ");
                        code.append(new Java(ast.getChildren().get(1)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case FACTOR -> {
                switch (ast.getSubType()) {
                    case POSITIVE -> {
                        code.append("+");
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                    }
                    case NEGATIVE -> {
                        code.append("-");
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                    }
                    case PAR -> {
                        code.append("(");
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                        code.append(")");
                    }
                    case DEFAULT -> {
                        code.append(new Java(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case ID, NUM -> {
                code.append(ast.getLexeme());
            }
        }
        return code.toString();
    }

    private void handleOptions(AbstractSyntaxTree statements, AbstractSyntaxTree options, StringBuilder code) {
        if(options.getSubType().equals(RuleType.SINGLE)) {
            code.append("case ");
            code.append(new Java(options.getChildren().get(0)).generateCode());
            code.append(":\n");
            changeFollowStatementToStatement(statements, code);
        }
        else if (options.getSubType().equals(RuleType.MULTI)){
            handleOptions(statements, options.getChildren().get(0), code);
            code.append("\ncase ");
            code.append(new Java(options.getChildren().get(1)).generateCode());
            code.append(":\n");
            changeFollowStatementToStatement(statements, code);
        }
    }

    private void changeFollowStatementToStatement(AbstractSyntaxTree followStatements, StringBuilder code) {
        if(!followStatements.getSubType().equals(RuleType.EMPTY)) {
            code.append(new Java(followStatements.getChildren().get(0)).generateCode());
        }
    }
}
