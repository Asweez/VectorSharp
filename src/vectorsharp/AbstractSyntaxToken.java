package vectorsharp;

public abstract class AbstractSyntaxToken {
	public abstract String toString();
	
	public static class NumberExprToken extends AbstractSyntaxToken{
		public double value;
		public NumberExprToken(double value){
			this.value = value;
		}
		public String toString(){
			return "number: " + value;
		}
	}
	
	public static class VariableExprToken extends AbstractSyntaxToken{
		public String value;
		public VariableExprToken(String value){
			this.value = value;
		}
		public String toString(){
			return "variable: " + value;
		}
	}
	
	public static class BinaryOpToken extends AbstractSyntaxToken{
		public VectorSharpCompiler.Token value;
		public AbstractSyntaxToken lhs, rhs;
		public BinaryOpToken(VectorSharpCompiler.Token value, AbstractSyntaxToken lhs, AbstractSyntaxToken rhs){
			this.value = value;
			this.lhs = lhs;
			this.rhs = rhs;
		}
		public String toString(){
			return "op: " + value;
		}
	}
	
	public static class VectorExprToken extends AbstractSyntaxToken{
		public Vector value;
		public VectorExprToken(Vector value){
			this.value = value;
		}
		public String toString(){
			return "vector: " + value;
		}
	}
	
	public static class SubFieldExprToken extends AbstractSyntaxToken{
		public String subfield;
		public AbstractSyntaxToken.VariableExprToken lhs;
		
		public SubFieldExprToken(String subfield, AbstractSyntaxToken.VariableExprToken lhs) {
			this.subfield = subfield;
			this.lhs = lhs;
		}


		@Override
		public String toString() {
			return "subfield: ";
		}
		
	}
	
//	public static class FunctionCallToken extends AbstractSyntaxToken{
//		
//	}
}
