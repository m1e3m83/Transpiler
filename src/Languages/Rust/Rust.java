package Languages.Rust;

import Languages.C.C;
import Languages.Code;
import Languages.Java.Java;
import Transpiler.AbstractSyntaxTree;
import Transpiler.RuleType;

import java.io.StringReader;

public class Rust extends Code {

    public Rust(String code) {
        super(code);
    }

    public Rust(AbstractSyntaxTree ast) {
        super(ast);
    }

    @Override
    public AbstractSyntaxTree parseToAST() {
        /**
         * This function parses the given program code with Rust Parser.
         * @return  Parsed AST (Abstract Syntax Tree) of the given Program.
         */
        RustLexer lexer = new RustLexer(new StringReader(this.code));
        RustParser parser = new RustParser(lexer);
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
         * @return  The generated Rust program code for the given AST.
         */
        StringBuilder code = new StringBuilder();
        switch (ast.getType()) {
            case PROGRAM -> {
                code.append("fn ");
                code.append(new Rust(ast.getChildren().getFirst()).generateCode());
                code.append("() {\n");
                code.append(new Rust(ast.getChildren().get(1)).generateCode());
                code.append("}");
            }
            case STATEMENTS -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new Rust(ast.getChildren().getFirst()).generateCode());
                        code.append(new Rust(ast.getChildren().get(1)).generateCode());
                        code.append("\n");
                    }
                    case SINGLE -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                        code.append("\n");
                    }
                }
            }
            case FOLLOW_STATEMENTS -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append("{\n");
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                        code.append("}");
                        code.append("\n");
                    }
                    case SINGLE -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                    }
                    case EMPTY -> {
                        code.append("{}\n");
                    }
                }
            }
            case STATEMENT -> {
                switch (ast.getSubType()) {
                    case DECLARE, ASSIGNMENTS -> {
                        code.append(new Rust(ast.getChildren().getFirst()).generateCode());
                        code.append(";");
                    }
                    case BREAK -> code.append("break;");
                    case CONTINUE -> code.append("continue;");
                    case PRINT -> {
                        code.append("println!(");
                        code.append(new Rust(ast.getChildren().getFirst()).generateCode());
                        code.append(");");
                    }
                    case IF, SWITCH, WHILE -> {
                        code.append(new Rust(ast.getChildren().getFirst()).generateCode());
                    }
                }
            }
            case PRINT -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                        code.append(";\nprintln!(");
                        code.append(new Rust(ast.getChildren().get(1)).generateCode());
                        code.append(")");
                    }
                    case SINGLE -> {
                        code.append(new Rust(ast.getChildren().getFirst()).generateCode());
                    }
                }
            }
            case DECLARATION -> {
                AbstractSyntaxTree assignments = ast.getChildren().get(0);
                createDeclarations(assignments, code);
            }
            case ASSIGNMENTS -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                        code.append(";\n");
                        code.append(new Rust(ast.getChildren().get(1)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case ASSIGNMENT -> {
                code.append(new Rust(ast.getChildren().get(0)).generateCode());
                code.append(" = ");
                code.append(new Rust(ast.getChildren().get(1)).generateCode());
            }
            case IF -> {
                code.append("if (");
                code.append(new Rust(ast.getChildren().get(0)).generateCode());
                code.append(" ) ");
                code.append(new Rust(ast.getChildren().get(1)).generateCode());
                code.append("else ");
                code.append(new Rust(ast.getChildren().get(2)).generateCode());
            }
            case SWITCH -> {
                code.append("match ");
                code.append(new Rust(ast.getChildren().get(0)).generateCode());
                code.append(" {\n");
                code.append(new Rust(ast.getChildren().get(1)).generateCode());
                code.append("}\n");
            }
            case CASES -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                        code.append("=>");
                        code.append(new Rust(ast.getChildren().get(1)).generateCode());
                        code.append(" ,\n");
                        code.append(new Rust(ast.getChildren().get(2)).generateCode());
                    }
                    case DEFAULT -> {
                        code.append("_ => ");
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());

                    }
                }
            }
            case OPTIONS -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                        code.append(" | ");
                        code.append(new C(ast.getChildren().get(1)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new C(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case WHILE -> {
                code.append("while (");
                code.append(new Rust(ast.getChildren().get(0)).generateCode());
                code.append(") ");
                code.append(new Rust(ast.getChildren().get(1)).generateCode());
            }
            case DISJUNCTION -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                        code.append(" || ");
                        code.append(new Rust(ast.getChildren().get(1)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case CONJUNCTION -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                        code.append(" && ");
                        code.append(new Rust(ast.getChildren().get(1)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case INVERSION -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append("! ");
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case COMPARISON, PRIMARY -> {
                code.append(new Rust(ast.getChildren().get(0)).generateCode());
            }
            case EQ -> {
                code.append(new Rust(ast.getChildren().get(0)).generateCode());
                code.append(" == ");
                code.append(new Rust(ast.getChildren().get(1)).generateCode());
            }
            case LT -> {
                code.append(new Rust(ast.getChildren().get(0)).generateCode());
                code.append("<");
                code.append(new Rust(ast.getChildren().get(1)).generateCode());
            }
            case GT -> {
                code.append(new Rust(ast.getChildren().get(0)).generateCode());
                code.append(">");
                code.append(new Rust(ast.getChildren().get(1)).generateCode());
            }
            case SUM -> {
                switch (ast.getSubType()) {
                    case ADD -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                        code.append(" + ");
                        code.append(new Rust(ast.getChildren().get(1)).generateCode());
                    }
                    case SUB -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                        code.append(" - ");
                        code.append(new Rust(ast.getChildren().get(1)).generateCode());
                    }
                    case DEFAULT -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case TERM -> {
                switch (ast.getSubType()) {
                    case TIMES -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                        code.append(" * ");
                        code.append(new Rust(ast.getChildren().get(1)).generateCode());
                    }
                    case DIVIDES -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                        code.append(" / ");
                        code.append(new Rust(ast.getChildren().get(1)).generateCode());
                    }
                    case DEFAULT -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case MODULO -> {
                switch (ast.getSubType()) {
                    case MULTI -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                        code.append(" % ");
                        code.append(new Rust(ast.getChildren().get(1)).generateCode());
                    }
                    case SINGLE -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case FACTOR -> {
                switch (ast.getSubType()) {
                    case POSITIVE -> {
                        code.append("+");
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                    }
                    case NEGATIVE -> {
                        code.append("-");
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                    }
                    case PAR -> {
                        code.append("(");
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                        code.append(")");
                    }
                    case DEFAULT -> {
                        code.append(new Rust(ast.getChildren().get(0)).generateCode());
                    }
                }
            }
            case ID, NUM -> {
                code.append(ast.getLexeme());
            }
        }
        return code.toString();
    }

    private void createDeclarations(AbstractSyntaxTree assignments, StringBuilder code) {
        if(assignments.getSubType().equals(RuleType.SINGLE)) {
            code.append("let ");
            code.append(new Rust(assignments.getChildren().get(0)).generateCode());
        }
        else if(assignments.getSubType().equals(RuleType.MULTI)) {
            createDeclarations(assignments.getChildren().get(0), code);
            code.append(";\nlet ");
            code.append(new Rust(assignments.getChildren().get(1)).generateCode());
        }
    }
}
