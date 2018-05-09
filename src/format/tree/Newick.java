/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.tree;

/**
 *
 * @author feilu
 */
public class Newick {
    TreeNode<NodeWHeight> root = new TreeNode<>(new NodeWHeight("Root", 0));
    
    public Newick (String nwkS) {
        nwkS = "(B:6.0,(A:5.0,C:3.0,E:4.0):5.0,D:11.0)";
        //nwkS = "((raccoon:19.19959,bear:6.80041):0.84600,((sea_lion:11.99700, seal:12.00300):7.52973,((monkey:100.85930,cat:47.14069):20.59201, weasel:18.87953):2.09460):3.87382,dog:25.46154);";
        this.ParseNwk(nwkS, root);
    }
    
    private void ParseNwk (String nwkS, TreeNode<NodeWHeight> parent) {
        if (nwkS.endsWith(";")) nwkS = nwkS.replaceFirst(";", "");
        if (nwkS.startsWith("(") && nwkS.endsWith(")")) {
            nwkS = nwkS.substring(1, nwkS.length()-1);
        }
        int cnt = 0;
        int currentIndex = 0;
        for (int i = 0; i < nwkS.length(); i++) {
            if (nwkS.charAt(i) == '(') cnt++;
            else if (nwkS.charAt(i) == ')') cnt--;
            else if (nwkS.charAt(i) == ',') {
                if (cnt == 0) {
                    String currentNwkS = nwkS.substring(currentIndex, i);
                    currentIndex = i+1;
                    System.out.println(currentNwkS);
                    if (currentNwkS.contains(")")) {
                        int cIndex = currentNwkS.length()-1;
                        for (int j = 0; j < currentNwkS.length(); j++) {
                            if (currentNwkS.charAt(cIndex) == ':') break;
                            cIndex--;
                        }
                        TreeNode<NodeWHeight> child = new TreeNode<>(new NodeWHeight(currentNwkS.substring(0, cIndex), Double.parseDouble(currentNwkS.substring(cIndex+1, currentNwkS.length()))));
                        parent.addChild(child);
                        //this.ParseNwk(currentNwkS, child);
                    }
                }
            }
            if (i == nwkS.length()-1) {
                String currentNwkS = nwkS.substring(currentIndex, i+1);
                System.out.println(currentNwkS);
                if (currentNwkS.contains(")")) {
                    int cIndex = currentNwkS.length()-1;
                    for (int j = 0; j < currentNwkS.length(); j++) {
                        if (currentNwkS.charAt(cIndex) == ':') break;
                        cIndex--;
                    }
                    TreeNode<NodeWHeight> child = new TreeNode<>(new NodeWHeight(currentNwkS.substring(0, cIndex), Double.parseDouble(currentNwkS.substring(cIndex+1, currentNwkS.length()))));
                    parent.addChild(child);
                    //this.ParseNwk(currentNwkS, child);
                }
            }
        }
        
        
        
    }
}
