function Link(el)
  if string.match(el.target, "%.md") then
    el.target = string.gsub(el.target, "%.md", ".html")
    el.target = string.lower(el.target)
    el.target = string.gsub(el.target, "_", "-")
  end
  return el
end
