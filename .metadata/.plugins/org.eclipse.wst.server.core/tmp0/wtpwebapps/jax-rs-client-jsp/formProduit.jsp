<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="iset.master.spring.model.*" %>
<%
    Produit produit = (Produit) request.getAttribute("produit"); // Récupérer le produit s'il existe
%>
<!DOCTYPE html>
<html>
<head>
    <title><%= (produit != null && produit.getId() != null) ? "Modifier Produit" : "Ajouter Produit" %></title>
</head>
<body>
    <h2><%= (produit != null && produit.getId() != null) ? "Modifier le Produit" : "Ajouter un Nouveau Produit" %></h2>

    <form action="saveProduit" method="post">
        <% if (produit != null && produit.getId() != null) { %>
            <input type="hidden" name="id" value="<%= produit.getId() %>">
        <% } %>

        <label for="designation">Désignation :</label>
        <input type="text" name="designation" id="designation" value="<%= (produit != null) ? produit.getDesignation() : "" %>" required>
        <br><br>

        <label for="prix">Prix :</label>
        <input type="number" step="0.01" name="prix" id="prix" value="<%= (produit != null) ? produit.getPrix() : "" %>" required>
        <br><br>

        <label for="quantite">Quantité :</label>
        <input type="number" name="quantite" id="quantite" value="<%= (produit != null) ? produit.getQuantite() : "" %>" required>
        <br><br>

        <label for="dateAchat">Date d'Achat :</label>
        <input type="date" name="dateAchat" id="dateAchat" value="<%= (produit != null && produit.getDateAchat() != null) ? produit.getDateAchat().toString() : "" %>">
        <br><br>

        <button type="submit">Enregistrer</button>
    </form>

    <br>
    <a href="listProduits.jsp">Retour à la liste des produits</a>
</body>
</html>
