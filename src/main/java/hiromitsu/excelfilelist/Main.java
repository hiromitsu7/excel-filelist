package hiromitsu.excelfilelist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class Main {
    public static void main(String[] args) throws IOException {

        Path path = Paths.get("find.txt");
        List<String> lines = Files.readAllLines(path);
        Node root = new Node();
        root.setType("root");
        root.setSimpleName("ROOT");

        for (String line : lines) {
            NodeFactory.addNode(root, line);
        }

        root.accept(new Visitor());
    }
}

class Visitor {
    private int depth = 0;

    void visit(Node node) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("\t");
        }

        System.out.println(sb.toString() + node.getSimpleName());

        depth++;
        for (Node child : node.getChildren()) {
            child.accept(this);
        }
        depth--;
    }
}

@Getter
@Setter
class Node {
    private String type;
    private String simpleName;
    private Node parent;
    private List<Node> children = new ArrayList<>();

    /**
     * 追加する
     * 
     * @param child
     * @return 追加に成功した場合は追加したNodeを返す。重複のために追加に失敗した場合は既存のNodeを返す
     */
    Node addChild(Node child) {
        int index = children.indexOf(child);
        if (index < 0) {
            children.add(child);
            child.setParent(this);
            return child;
        } else {
            return children.get(index);
        }
    }

    void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Node) {
            Node node = (Node) o;
            return this.simpleName.equals(node.getSimpleName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return simpleName.hashCode();
    }

}

class NodeFactory {
    private NodeFactory() {
        // hide constructor
    }

    static void addNode(Node root, String leafString) {
        String[] split = leafString.split("/");
        Node pointedNode = root;

        for (int i = 0; i < split.length; i++) {
            String part = split[i];

            // カレントディレクトリの"."のみだった場合は何もしない
            if (part.equals("."))
                continue;

            Node node = new Node();
            node.setSimpleName(part);

            if (split.length - 1 == i) {
                // 最後の要素の場合
                node.setType("file");
                pointedNode.addChild(node);
                pointedNode = node;
            } else {
                // 途中の要素の場合
                node.setType("directory");
                // 追加できなかった既存のノード、追加できたら追加したノードを取得してそれをpointedNodeにする
                Node added = pointedNode.addChild(node);
                pointedNode = added;
            }
        }
    }
}
