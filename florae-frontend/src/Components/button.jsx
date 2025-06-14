/**
 * Button is a reusable component that renders a styled button element.
 * It supports optional icons and all standard button props via rest parameters.
 *
 * @component
 * @example
 * // Simple usage
 * <Button buttonText="Submit" onClick={handleSubmit} />
 *
 * // With an icon
 * <Button buttonText="Copy" icon={<FiCopy />} onClick={handleCopy} />
 *
 * @param {Object} props - Component props
 * @param {string} props.buttonText - Text to display inside the button
 * @param {JSX.Element} [props.icon] - Optional icon element displayed before the text
 * @param {Object} [props.className] - Additional class names for styling
 * @param {React.ButtonHTMLAttributes<HTMLButtonElement>} props - Inherits all native button attributes (e.g. onClick, type)
 *
 * @returns {JSX.Element} A customizable button with optional icon
 */

export default function Button({ buttonText, icon, ...props }) {
  return (
    <button {...props} className={`flex items-center ${props.className || ''}`}>
      {icon && <span className="mr-2">{icon}</span>}
      {buttonText}
    </button>
  );
}
