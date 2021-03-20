package dk.aau.cs.TCTL;

public class LTLAUNode extends TCTLAUNode {
    private TCTLAbstractStateProperty left;
    private TCTLAbstractStateProperty right;

    public LTLAUNode(TCTLAbstractStateProperty left, TCTLAbstractStateProperty right) {
        this.left = left;
        this.right = right;
        this.left.setParent(this);
        this.right.setParent(this);
    }

    @Override
    public String toString() {
        String leftString = left.isSimpleProperty() ? left.toString() : "("
            + left.toString() + ")";
        String rightString = right.isSimpleProperty() ? right.toString() : "("
            + right.toString() + ")";

        return leftString + " U " + rightString;
    }

    @Override
    public StringPosition[] getChildren() {
        int leftStart = left.isSimpleProperty() ? 0 : 1;
        leftStart  += 0;
        int leftEnd = leftStart + left.toString().length();
        StringPosition leftPos = new StringPosition(leftStart, leftEnd, left);

        int rightStart = right.isSimpleProperty() ? 0 : 1;
        rightStart += leftEnd + 3 + + (left.isSimpleProperty() ? 0 : 1);
        int rightEnd = rightStart + right.toString().length();
        StringPosition rightPos = new StringPosition(rightStart, rightEnd, right);

        StringPosition[] children = { leftPos, rightPos };
        return children;
    }

    @Override
    public TCTLAbstractPathProperty replace(TCTLAbstractProperty object1,
                                            TCTLAbstractProperty object2) {
        if (this == object1 && object2 instanceof TCTLAbstractPathProperty) {
            return (TCTLAbstractPathProperty) object2;
        } else {
            left = left.replace(object1, object2);
            right = right.replace(object1, object2);
            return this;
        }
    }

    @Override
    public boolean containsPlaceHolder() {
        return left.containsPlaceHolder() || right.containsPlaceHolder();
    }
}
