package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Evaluator implements Transform {

    private LinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new LinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues = new LinkedList<>();
        applyStylesheet(ast.root);
    }

    private void applyStylesheet(Stylesheet node) {
        variableValues.addFirst(new HashMap<>());
        for (ASTNode child : node.getChildren()) {
            if (child instanceof Stylerule) {
                applyStyleRule((Stylerule) child);
            } else if (child instanceof VariableAssignment) {
                applyAssignment((VariableAssignment) child);
            }
        }
        variableValues.removeFirst();
    }

    private void applyStyleRule(Stylerule node) {
        variableValues.addFirst(new HashMap<>());
        for (ASTNode child : node.getChildren()) {
            if (child instanceof Declaration) {
                applyDeclaration((Declaration) child);
            } else if (child instanceof Stylerule) {
                applyStyleRule((Stylerule) child);
            } else if (child instanceof VariableAssignment) {
                applyAssignment((VariableAssignment) child);
            } else if (child instanceof IfClause) {
                applyIfClause((IfClause) child);
            }
        }
        variableValues.removeFirst();
    }

    private void applyDeclaration(Declaration node) {
        node.expression = evalExpression(node.expression);
    }

    private void applyAssignment(VariableAssignment node) {
        Literal value = evalExpression(node.expression);
        node.expression = value;
        variableValues.getFirst().put(node.name.name, value);
    }

    private void applyIfClause(IfClause node) {
        node.conditionalExpression = evalExpression(node.conditionalExpression);
        applyClauseBody(node.body);
        if (node.elseClause != null) {
            applyClauseBody(node.elseClause.body);
        }
    }

    private void applyClauseBody(List<ASTNode> body) {
        variableValues.addFirst(new HashMap<>());
        for (ASTNode child : body) {
            if (child instanceof Declaration) {
                applyDeclaration((Declaration) child);
            } else if (child instanceof VariableAssignment) {
                applyAssignment((VariableAssignment) child);
            } else if (child instanceof IfClause) {
                applyIfClause((IfClause) child);
            }
        }
        variableValues.removeFirst();
    }

    private Literal evalExpression(Expression node) {
        if (node instanceof Literal) {
            return (Literal) node;
        }

        if (node instanceof VariableReference) {
            VariableReference variableReference = (VariableReference) node;
            for (HashMap<String, Literal> scope : variableValues) {
                if (scope.containsKey(variableReference.name)) {
                    return scope.get(variableReference.name);
                }
            }
            throw new IllegalArgumentException("Variable " + variableReference.name + " is niet gedefinieerd in de scope.");
        }   

        if (node instanceof Operation) {
            Operation operation = (Operation) node;

            Literal left = evalExpression(operation.lhs);
            Literal right = evalExpression(operation.rhs);

            int leftValue = getLiteralValue(left);
            int rightValue = getLiteralValue(right);
            int result;

            if (node instanceof AddOperation) {
                result = leftValue + rightValue;
            } else if (node instanceof SubtractOperation) {
                result = leftValue - rightValue;
            } else if (node instanceof MultiplyOperation) {
                result = leftValue * rightValue;
            } else {
                throw new IllegalArgumentException("Operation is ongeldig:" + leftValue + "&" + rightValue);
            }

            if (left instanceof PixelLiteral || right instanceof PixelLiteral) {
                return new PixelLiteral(result);
            } else if (left instanceof PercentageLiteral || right instanceof PercentageLiteral) {
                return new PercentageLiteral(result);
            } else {
                return new ScalarLiteral(result);
            }
        }
        throw new IllegalArgumentException("Ongeldige Expression");
    }

    private int getLiteralValue(Literal node) {
        if (node instanceof PixelLiteral) {
            return ((PixelLiteral) node).value;
        } else if (node instanceof PercentageLiteral) {
            return ((PercentageLiteral) node).value;
        } else if (node instanceof ScalarLiteral) {
            return ((ScalarLiteral) node).value;
        }
        throw new IllegalArgumentException("Literal heeft geen value");
    }
}
