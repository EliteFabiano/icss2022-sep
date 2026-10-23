package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.VariableAssignment;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.Operation;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

	// Accumulator attributes:
	private AST ast;

	// Use this to keep track of the parent nodes when recursively traversing the
	// ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}

	public AST getAST() {
		return ast;
	}

	// Stylesheet
	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.push(ast.root);
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.pop();
	}

	// Stylerule
	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		currentContainer.push(new Stylerule());
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		ASTNode stylerule = currentContainer.pop();
		currentContainer.peek().addChild(stylerule);
	}

	// Selector
	@Override
	public void enterTagSelector(ICSSParser.TagSelectorContext ctx) {
		currentContainer.push(new TagSelector(ctx.getText()));
	}

	@Override
	public void exitTagSelector(ICSSParser.TagSelectorContext ctx) {
		ASTNode selector = currentContainer.pop();
		currentContainer.peek().addChild(selector);
	}

	@Override
	public void enterIdSelector(ICSSParser.IdSelectorContext ctx) {
		currentContainer.push(new IdSelector(ctx.getText()));
	}

	@Override
	public void exitIdSelector(ICSSParser.IdSelectorContext ctx) {
		ASTNode selector = currentContainer.pop();
		currentContainer.peek().addChild(selector);
	}

	@Override
	public void enterClassSelector(ICSSParser.ClassSelectorContext ctx) {
		currentContainer.push(new ClassSelector(ctx.getText()));
	}

	@Override
	public void exitClassSelector(ICSSParser.ClassSelectorContext ctx) {
		ASTNode selector = currentContainer.pop();
		currentContainer.peek().addChild(selector);
	}

	// Declaration
	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		currentContainer.push(new Declaration(ctx.propertyName().getText()));
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		ASTNode declaration = currentContainer.pop();
		currentContainer.peek().addChild(declaration);
	}

	// Variable
	@Override
	public void enterVariable_declaration(ICSSParser.Variable_declarationContext ctx) {
		VariableAssignment assignment = new VariableAssignment();
		assignment.name = new VariableReference(ctx.CAPITAL_IDENT().getText());
		currentContainer.push(assignment);
	}

	@Override
	public void exitVariable_declaration(ICSSParser.Variable_declarationContext ctx) {
		ASTNode assignment = currentContainer.pop();
		currentContainer.peek().addChild(assignment);
	}

	@Override
	public void enterVariable_reference(ICSSParser.Variable_referenceContext ctx) {
		currentContainer.push(new VariableReference(ctx.getText()));
	}

	@Override
	public void exitVariable_reference(ICSSParser.Variable_referenceContext ctx) {
		ASTNode reference = currentContainer.pop();
		currentContainer.peek().addChild(reference);
	}

	// Expression
	@Override
	public void enterExpr(ICSSParser.ExprContext ctx) {
		if (ctx.expr().size() != 2) {
			return;
		}

		Operation operation;
		if (ctx.MUL() != null) {
			operation = new MultiplyOperation();
		} else if (ctx.PLUS() != null) {
			operation = new AddOperation();
		} else {
			operation = new SubtractOperation();
		}
		currentContainer.push(operation);
	}

	@Override
	public void exitExpr(ICSSParser.ExprContext ctx) {
		if (ctx.expr().size() == 2) {
			ASTNode operation = currentContainer.pop();
			currentContainer.peek().addChild(operation);
		}
	}

	// Literal
	@Override
	public void enterColorLiteral(ICSSParser.ColorLiteralContext ctx) {
		currentContainer.push(new ColorLiteral(ctx.getText()));
	}

	@Override
	public void exitColorLiteral(ICSSParser.ColorLiteralContext ctx) {
		ASTNode literal = currentContainer.pop();
		currentContainer.peek().addChild(literal);
	}

	@Override
	public void enterPixelLiteral(ICSSParser.PixelLiteralContext ctx) {
		currentContainer.push(new PixelLiteral(ctx.getText()));
	}

	@Override
	public void exitPixelLiteral(ICSSParser.PixelLiteralContext ctx) {
		ASTNode literal = currentContainer.pop();
		currentContainer.peek().addChild(literal);
	}

	@Override
	public void enterPercentageLiteral(ICSSParser.PercentageLiteralContext ctx) {
		currentContainer.push(new PercentageLiteral(ctx.getText()));
	}

	@Override
	public void exitPercentageLiteral(ICSSParser.PercentageLiteralContext ctx) {
		ASTNode literal = currentContainer.pop();
		currentContainer.peek().addChild(literal);
	}

	@Override
	public void enterBooleanLiteral(ICSSParser.BooleanLiteralContext ctx) {
		currentContainer.push(new BoolLiteral(ctx.getText()));
	}

	@Override
	public void exitBooleanLiteral(ICSSParser.BooleanLiteralContext ctx) {
		ASTNode literal = currentContainer.pop();
		currentContainer.peek().addChild(literal);
	}

	@Override
	public void enterScalarLiteral(ICSSParser.ScalarLiteralContext ctx) {
		currentContainer.push(new ScalarLiteral(ctx.getText()));
	}

	@Override
	public void exitScalarLiteral(ICSSParser.ScalarLiteralContext ctx) {
		ASTNode literal = currentContainer.pop();
		currentContainer.peek().addChild(literal);
	}
}
