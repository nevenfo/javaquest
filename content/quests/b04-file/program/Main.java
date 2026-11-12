void main() {
    java.util.LinkedList<String> file = new java.util.LinkedList<>();
    file.add("B");
    file.addFirst("A");
    file.addLast("C");
    file.add(1, "X");
    IO.println(file);
    IO.println(file.removeFirst() + file.removeLast());
    IO.println(file.peekFirst());
    file.push("Z");
    IO.println(file + " " + file.size());
    IO.println(file.pop() + file.indexOf("B"));
}
