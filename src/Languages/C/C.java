package Languages.C;

import Languages.Code;
import Transpiler.AbstractSyntaxTree;
import Transpiler.NodeType;
import Transpiler.RuleType;

import java.io.StringReader;

public class C extends Code {

    public C(String code) {
        super(code);
    }

    public C(AbstractSyntaxTree ast) {
        super(ast);
    }

    @Override
    public AbstractSyntaxTree parseToAST() {
         /**
         * This function parses the given program code with C Parser.
         * @return  Parsed AST (Abstract Syntax Tree) of the given Program.
         */
        CLexer lexer = new CLexer(new StringReader(this.code));
        CParser parser = new CParser(lexer);
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
                code.append("void ");
                code.append(new C(ast.getChildren().getFirst()).generateCode());
                code.append("() {\n");
                code.append(new C(ast.getChildren().get(1)).generateCode());
                code.append("}");
            }
            case STATEMENTS -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new C(ast.getChildren().getFirst()).generateCode());
                        code.append(new C(ast.getChildren().get(1)).generateCode());
                        code.append("\n");
                    }
                    case SINGLE -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                        code.append("\n");
                    }
                }
            }
            case FOLLOW_STATEMENTS -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append("{\n");
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                        code.append("}");
                        code.append("\n");
                    }
                    case SINGLE -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                    }
                    case EMPTY -> {
                        code.append("{}\n");
                    }
                }
            }
            case STATEMENT -> {
                switch (ast.getSubType()) {
                    case DECLARE, ASSIGNMENTS -> {
                        code.append(new C(ast.getChildren().getFirst()).generateCode());
                        code.append(";");
                    }
                    case BREAK -> code.append("break;");
                    case CONTINUE -> code.append("continue;");
                    case PRINT -> {
                        code.append("cout << ");
                        code.append(new C(ast.getChildren().getFirst()).generateCode());
                        code.append(" ;");
                    }
                    case IF, SWITCH, WHILE -> {
                        code.append(new C(ast.getChildren().getFirst()).generateCode());
                    }
                }
            }
            case PRINT -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                        code.append(" << ");
                        code.append(new C(ast.getChildren().get(1)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case DECLARATION -> {
                code.append("int ");
                code.append(new C(ast.getChildren().get(0)).generateCode());
            }
            case ASSIGNMENTS -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                        code.append(" , ");
                        code.append(new C(ast.getChildren().get(1)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case ASSIGNMENT -> {
                code.append(new C(ast.getChildren().get(0)).generateCode());
                code.append(" = ");
                code.append(new C(ast.getChildren().get(1)).generateCode());
            }
            case IF -> {
                code.append("if (");
                code.append(new C(ast.getChildren().get(0)).generateCode());
                code.append(" ) ");
                code.append(new C(ast.getChildren().get(1)).generateCode());
                code.append("else ");
                code.append(new C(ast.getChildren().get(2)).generateCode());
            }
            case SWITCH -> {
                code.append("switch (");
                code.append(new C(ast.getChildren().get(0)).generateCode());
                code.append(") {\n");
                code.append(new C(ast.getChildren().get(1)).generateCode());
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
                            code.append(new C(ast.getChildren().get(0)).generateCode());
                            code.append(" : ");
                            code.append(new C(ast.getChildren().get(1)).generateCode());
                        }
                        code.append(new C(ast.getChildren().get(2)).generateCode());
                    }
                    case DEFAULT -> {
                        code.append("default : ");
                        code.append(new C(ast.getChildren().get(0)).generateCode());

                    }
                }
            }
            case WHILE -> {
                code.append("while (");
                code.append(new C(ast.getChildren().get(0)).generateCode());
                code.append(") ");
                code.append(new C(ast.getChildren().get(1)).generateCode());
            }
            case DISJUNCTION -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                        code.append(" || ");
                        code.append(new C(ast.getChildren().get(1)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case CONJUNCTION -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                        code.append(" && ");
                        code.append(new C(ast.getChildren().get(1)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case INVERSION -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append("!");
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case COMPARISON, PRIMARY -> {
                code.append(new C(ast.getChildren().get(0)).generateCode());
            }
            case EQ -> {
                code.append(new C(ast.getChildren().get(0)).generateCode());
                code.append(" == ");
                code.append(new C(ast.getChildren().get(1)).generateCode());
            }
            case LT -> {
                code.append(new C(ast.getChildren().get(0)).generateCode());
                code.append("<");
                code.append(new C(ast.getChildren().get(1)).generateCode());
            }
            case GT -> {
                code.append(new C(ast.getChildren().get(0)).generateCode());
                code.append(">");
                code.append(new C(ast.getChildren().get(1)).generateCode());
            }
            case SUM -> {
                switch (ast.getSubType()) {
                    case ADD -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                        code.append(" + ");
                        code.append(new C(ast.getChildren().get(1)).generateCode());
                    }
                    case SUB -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                        code.append(" - ");
                        code.append(new C(ast.getChildren().get(1)).generateCode());
                    }
                    case DEFAULT -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case TERM -> {
                switch (ast.getSubType()) {
                    case TIMES -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                        code.append(" * ");
                        code.append(new C(ast.getChildren().get(1)).generateCode());
                    }
                    case DIVIDES -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                        code.append(" / ");
                        code.append(new C(ast.getChildren().get(1)).generateCode());
                    }
                    case DEFAULT -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case MODULO -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                        code.append(" % ");
                        code.append(new C(ast.getChildren().get(1)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case FACTOR -> {
                switch (ast.getSubType()) {
                    case POSITIVE -> {
                        code.append("+");
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                    }
                    case NEGATIVE -> {
                        code.append("-");
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                    }
                    case PAR -> {
                        code.append("(");
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                        code.append(")");
                    }
                    case DEFAULT -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
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
            code.append(new C(options.getChildren().get(0)).generateCode());
            code.append(":\n");
            changeFollowStatementToStatement(statements, code);
        }
        else if (options.getSubType().equals(RuleType.MULTI)){
            handleOptions(statements, options.getChildren().get(0), code);
            code.append("\ncase ");
            code.append(new C(options.getChildren().get(1)).generateCode());
            code.append(":\n");
            changeFollowStatementToStatement(statements, code);
        }
    }

    private void changeFollowStatementToStatement(AbstractSyntaxTree followStatements, StringBuilder code) {
        if(!followStatements.getSubType().equals(RuleType.EMPTY)) {
            code.append(new C(followStatements.getChildren().get(0)).generateCode());
        }
    }
}
