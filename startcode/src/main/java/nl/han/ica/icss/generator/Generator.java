package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.Stylesheet;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;

public class Generator {

	public String generate(AST ast) {
		return generateStylesheet(ast.root);
	}

	private String generateStylesheet(Stylesheet stylesheet) {
		StringBuilder css = new StringBuilder();
		for (ASTNode child : stylesheet.getChildren()) {
			if (child instanceof Stylerule) {
				css.append(generateStyleRule((Stylerule) child));
			}
		}
		return css.toString();
	}

	private String generateStyleRule(Stylerule rule) {
		StringBuilder css = new StringBuilder();
		css.append(rule.selectors.get(0)).append(" {\n");
		for (ASTNode child : rule.body) {
			if (child instanceof Declaration) {
				css.append("  ").append(generateDeclaration((Declaration) child)).append('\n');
			}
		}
		css.append("}\n");
		return css.toString();
	}

	private String generateDeclaration(Declaration declaration) {
		return declaration.property.name + ": " + getLiteralValue(declaration.expression) + ";";
	}

	private String getLiteralValue(Expression expression) {
		if (expression instanceof ColorLiteral) {
			return ((ColorLiteral) expression).value;
		}
		if (expression instanceof PixelLiteral) {
			return ((PixelLiteral) expression).value + "px";
		}
		if (expression instanceof PercentageLiteral) {
			return ((PercentageLiteral) expression).value + "%";
		}
		throw new IllegalArgumentException("Ongeldige Literal");
	}
}
