<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="iset.master.spring.model.Produit" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Liste des Produits</title>
    <!-- Bootstrap CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
            padding: 20px;
        }
        .card {
            transition: transform 0.2s;
        }
        .card:hover {
            transform: scale(1.05);
        }
    </style>
</head>
<body>

    <div class="container">
        <h2 class="text-center my-4">Liste des Produits</h2>

        <!-- Bouton Ajouter un produit -->
        <div class="text-end mb-4">
<a href="formProduit.jsp" class="btn btn-success">+ Ajouter un Produit</a>
        </div>

        <div class="row">
            <% 
                List<Produit> produits = (List<Produit>) request.getAttribute("produits");
                if (produits != null && !produits.isEmpty()) {
                    for (Produit p : produits) { 
            %>
            <div class="col-md-4 mb-4">
                <div class="card shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title"><%= p.getDesignation() %></h5>
                        <p class="card-text">
                            <strong>Prix :</strong> <%= p.getPrix() %> TND<br>
                            <strong>Quantité :</strong> <%= p.getQuantite() %><br>
                            <strong>Date d'Achat :</strong> <%= (p.getDateAchat() != null) ? p.getDateAchat().toString() : "Non spécifié" %>
                        </p>
                        <div class="d-flex justify-content-between">
                            <a href="ListProduitsAction?action=editer&id=<%= p.getId() %>" class="btn btn-primary btn-sm">Éditer</a>
                            <a href="ListProduitsAction?action=supprimer&id=<%= p.getId() %>" class="btn btn-danger btn-sm" onclick="return confirm('Voulez-vous vraiment supprimer ce produit ?');">Supprimer</a>
                        </div>
                    </div>
                </div>
            </div>
            <% } } else { %>
            <div class="col-12">
                <p class="text-center text-muted">Aucun produit disponible.</p>
            </div>
            <% } %>
        </div>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
