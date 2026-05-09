
function addLabel(btn) {
    const input= btn.closest('tr').querySelector('input[type="text"]');
    const value = input.value.trim();
    if (value) {
        var tr = renderTemplate('template-label', value);
        btn.closest('tbody').appendChild(tr);
    }
    input.value = '';
    input.focus();
}

function removeLabel(btn) {
    btn.closest('tr').remove();
}

function renderTemplate(id, label) {
    var template = document.getElementById(id);
    var container = document.createElement('template');
    container.innerHTML = template.innerHTML.trim().replaceAll("{label}", escapeHtml(label));
    return container.content.firstChild;
}

function escapeHtml(str) {
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}
