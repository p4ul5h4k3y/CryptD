public class ArgCheck {
    public ArgCheck(String[] newArgList){
        argList = newArgList;
    }

    public String[] argList;

    public boolean check(String toCheck) {
        int i = 0;

        for (String arg = argList[i]; i <= argList.length; i++) {
            if (toCheck.equals(arg)) {
                return true;
            }
        }

        return false;
    }
}