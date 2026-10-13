grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';

//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;

//Color value takes precedence over id idents
COLOR:
	'#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
PROPERTY_NAME: 'width' | 'height' | 'color' | 'background-color';
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

//--- PARSER: ---
stylesheet: stylerule* EOF;

stylerule: selector OPEN_BRACE declaration* CLOSE_BRACE;

declaration: propertyName COLON literal SEMICOLON;

propertyName: PROPERTY_NAME;

// Selector
selector: tagSelector | idSelector | classSelector;

tagSelector: LOWER_IDENT | PROPERTY_NAME | IF | ELSE;

idSelector: ID_IDENT | COLOR;

classSelector: CLASS_IDENT;

// Literal
literal: colorLiteral | percentageLiteral | pixelLiteral;

colorLiteral: COLOR;

percentageLiteral: PERCENTAGE;

pixelLiteral: PIXELSIZE;