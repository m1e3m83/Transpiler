package Transpiler;

import Languages.Runnable;

import java.util.ArrayList;
import java.util.List;

public class Transpiler <T extends Runnable> {

    List<T> runnables = new ArrayList<>();

    public void addCode(T code) {
        runnables.add(code);
    }

    public List<AbstractSyntaxTree> getAbstractSyntaxTrees() {
        /**
         * This function gathers each runnable object's abstract tree and returns them.
         * @return  list of each runnable object's abstract tree.
         */
        List<AbstractSyntaxTree> astList = new ArrayList<>();
        for(Runnable i: runnables) {
            astList.add(i.parseToAST());
        }
        return astList;
    }

    public List<String> getCodes() {
        /**
         * This function gathers each runnable object's string code and returns them.
         * @return  list of each runnable object's string code.
         */
        List<String> codeList = new ArrayList<>();
        for(Runnable i: runnables) {
            codeList.add(i.generateCode());
        }
        return codeList;
    }

    public List<T> getSimilarRunnables(T runnable) {
        /**
         * This function finds the runnable objects that are similar to the given runnable object
         * and returns them. Two runnable objects are considered similar only if their
         * Abstract Syntax Tree (AST) would be equal.
         * @param runnable: a runnable object (a specific programming language code)
         * @return          the runnable objects similar to the input.
         */
        List<T> list = new ArrayList<>();
        for(T i: runnables) {
            if(i.parseToAST().equals(runnable.parseToAST())) {
                list.add(i);
            }
        }
        return list;
    }

    public List<T> getUniqueRunnables() {
        /**
         * This function finds the runnable objects that are unique (non-similar) and returns them.
         * @return  list of unique runnable objects.
         */
        List<T> list = new ArrayList<>();
        outer :
        for(T i: runnables) {
            if(getSimilarRunnables(i).size() == 1) {
                list.add(i);
            }
        }
        return list;
    }
}
